package com.example.battery_management;
import com.fasterxml.jackson.core.type.TypeReference;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;


import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
public class ChargingService {

    private final WebClient webClient;

    private Disposable ongoingCharge;

    private final double maxBatteryCapacityKHW = 46.3;

    public ChargingService(WebClient.Builder webClientBuilder){
        this.webClient = webClientBuilder.baseUrl("http://127.0.0.1:5000").build();
    } //Hämtar servern via en webClient

    //GetMapping eftersom jag först använde Postman men ändrade till att göra en Java meny istället

    @GetMapping("/prices")
    public Mono<List<Double>> getPrice(){
        return webClient.get()
                .uri("/priceperhour")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(json -> JsonHelper.parse(json, new TypeReference<>() {}));
    } //En metod som hämtar alla priser per timme från servern

    @GetMapping("/baseload")
    public Mono<List<Double>> getBaseLoad(){
        return webClient.get()
                .uri("/baseload")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(json -> JsonHelper.parse(json, new TypeReference<>() {}));
    }//En metod som hämtar basen från servern

    public Mono<Void> startCharging(){
        return webClient.post()
                .uri("/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"charging\":\"on\"}")
                .retrieve()
                .bodyToMono(String.class)
                .then();
    } //den manuella starten för att börja ladda, gjord i början för att testa

    public Mono<Void> stopCharging(){
        return webClient.post()
                .uri("/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"charging\":\"off\"}")
                .retrieve()
                .bodyToMono(String.class)
                .then();
    } //den manuella stoppen för att stoppa laddningen, gjord i början för att testa

    public Mono<Double> getBatteryPercentage(){
        return webClient.get()
                .uri("/charge")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Double.class);
    } //hämtar laddnings procenten på bilen


    //ska bara ladda om priset är bra eller när elförbrukningen är som lägst
    public Mono<Void> smartCharging(boolean basedOnPrice) {
        return Mono.zip(getBaseLoad(), getPrice(), getLiveInfo())
                .flatMap(tuple -> {
                    List<Double> load = tuple.getT1();
                    List<Double> prices = tuple.getT2(); //bearbetar datan i flatMap, infon från price, load, info
                    InfoStatus info = tuple.getT3();

                    double batteryLevel = info.getBattery_capacity_kWh(); //batteriets ladding i kWh

                    int bestHour = -1;
                    double minValue = Double.MAX_VALUE;
                    double selectedChargingPower = 0.0;

                    //kollar igenom timmarna, för att hitta den med bäst pris
                    //jämför pris och förbrukning

                    for (int i = 0; i < 24; i++){
                        double current = basedOnPrice ? prices.get(i) : load.get(i);

                        double baseLoad = load.get(i);
                        double availablePower = 11.0 - baseLoad;
                        double chargingPower = Math.min(availablePower, 7.4);

                        if(chargingPower > 0 && current < minValue && batteryLevel < 80) {
                            minValue = current;
                            bestHour = i;
                            selectedChargingPower = chargingPower;
                        } //om den hittas en tid där det är minst förbrukning/bäst pris kommer den laddas upp till 80%
                    }

                    if (batteryLevel >= 80) {
                        System.out.println("Batteriet är redan 80% eller mer");
                        return Mono.empty();
                    } //säger till att det batteriet redan är 80% och laddar inte mer

                    //om batteriet inte är 80% och det är en timme (med det bästa priset) så startas laddningen
                    System.out.println("Startar laddning under timme: " + bestHour + " med effekt: " + selectedChargingPower + " kW");

                    double finalChargingPower = selectedChargingPower;

                  return startCharging()
                          .then(Mono.defer(() ->{
                  ongoingCharge = pollBatteryUntil(80, finalChargingPower).subscribe(); //du kan också stoppa innan 80 om du vill
                  return Mono.empty();
                  }));
                });
    }



    //kollar batteriet till det är 80%
    private Mono<Void> pollBatteryUntil(double targetPercent, double chargingPower) {
        return Flux.interval(Duration.ofSeconds(2))
                .flatMap(tick -> getLiveInfo())
                .map(info -> {

                    double currentCapacity = info.getBattery_capacity_kWh(); //den aktuella batterikapaciteten i kWh
                    double batteryPercent = (currentCapacity / maxBatteryCapacityKHW) * 100;
                    //jag delar den aktuella med max kapaciteten, och sedan gånger 100 för att få procenten

                    double totalLoad = info.getBase_current_load();

                    System.out.printf("Laddar... Batterinivå: %.2f%%4 | Total förbrukning: %.2f kW\n", batteryPercent, totalLoad);
                    return batteryPercent;
                })
                .takeUntil(percent -> percent >= targetPercent)
                .then(stopCharging()
                        .doOnSuccess(v ->
                    System.out.println("Laddning avslutades vid 80%"))); //när det nått 80% stoppas det
    }



    public void stopSmartCharging(){
        if (ongoingCharge != null && !ongoingCharge.isDisposed()) {
            ongoingCharge.dispose();
            stopCharging().subscribe();
            System.out.println("Smart laddning har stoppats manuellt.");
        } else {
            System.out.println("Ingen aktiv smart laddning att stoppa");
        }//Metod så du kan stoppa smart laddningen innan den är 80%
    }


    public Mono<Void> dischargeBattery(){
        Map<String, String> requestBody = Map.of("discharging","on");
        return webClient.post()
                .uri("/discharge")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSubscribe(sub -> System.out.println("Startar urladdning...."))
                .doOnSuccess(success -> System.out.println("Urladdadning klar"));
    }//startar om batteriet med en Post så det börjar på 20% igen

    @GetMapping("/liveinfo")
    public Mono<InfoStatus> getLiveInfo(){
        return webClient.get()
                .uri("/info")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(json -> JsonHelper.parse(json, new TypeReference<>() {}));
    }//hämtar live information från servern

}