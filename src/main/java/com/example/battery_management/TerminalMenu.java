package com.example.battery_management;

import java.util.List;
import java.util.Scanner;


//Menyn för att kunna välja att til exempel starta och stoppa laddningar, kunna ser information om priser och tider



public class TerminalMenu {

    private final ChargingService chargingService;
    private final Scanner scanner;

    public TerminalMenu(ChargingService chargingService){
        this.chargingService = chargingService;
        this.scanner = new Scanner(System.in);
    }

    public void show(){
        while (true) {
            System.out.println("----- HUVUDMENY -----");
            System.out.println("1. Visa elpriser per timme");
            System.out.println("2. Visa hushållets energiförbrukning");
            System.out.println("3. Visa aktuell status");
            System.out.println("4. Laddningsmeny");
            System.out.println("0. Avsluta");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    List<Double> prices = chargingService.getPrice().block();
                    System.out.println("Timme |     Pris (öre/kWh)");
                    System.out.println("----------------------");
                    for ( int i = 0; i < prices.size(); i++){
                        System.out.printf("%2d    | %10.2f\n", i, prices.get(i));  //för att det ska bli utskrivet snyggt
                    }
                    break;
                case "2":
                    chargingService.getBaseLoad()
                            .doOnNext(load -> {
                                System.out.println("Timme | Energiförbrukning (kWh)");
                                System.out.println("-------------------------------");
                                for (int i = 0; i < load.size(); i++){
                                    System.out.printf("%2d    |  %.2f\n", i, load.get(i));
                                }
                            })
                            .block();
                    break;
                case "3":
                    chargingService.getLiveInfo()
                            .doOnNext(info -> {
                                System.out.println("---- AKTUELL STATUS ----");
                                System.out.printf("Tid: %02d:%02d\n", info.getSim_time_hour(), info.getSim_time_min());
                                System.out.printf("Aktuell förbrukning: %.2f kW\n", info.getBase_current_load());
                                System.out.printf("Batterikapacitet: %.2f kWh\n", info.getBattery_capacity_kWh());
                                    })
                            .block();
                    break;
                case "4":
                    chargingMenu();
                    break;
                case "0":
                    System.out.println("Programmet avslutas.");
                    System.exit(0);
                    return;
                default:
                    System.out.println("Ogiltigt val. Försökt igen");
            }
        }
    }

    public void chargingMenu(){
        while (true){
            System.out.println("---- LADDNINGSMENY ----");
            System.out.println("1. Starta laddning (manuell enkel start)");
            System.out.println("3. Starta smart laddning (baserat på lägst hushållsförbrukning)");
            System.out.println("4. Starta smart laddning (baserat på lägst elpris)");
            System.out.println("--------------");
            System.out.println("5. Visa batterinivå");
            System.out.println("6. Starta om batteriet till 20%");
            System.out.println("--------------");
            System.out.println("8. Stoppa laddning (manuell enkel stop)");
            System.out.println("9. Stoppa laddning (smart)");
            System.out.println("--------------");
            System.out.println("0. Tillbaka till huvudmeny");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    chargingService.startCharging()
                            .doOnNext(response -> System.out.println("Laddning startad"))
                            .block();
                    break;
                case "3":
                    chargingService.smartCharging(false).block();
                    break;
                case "4":
                    chargingService.smartCharging(true).block();
                    break;
                case "5":
                    chargingService.getBatteryPercentage()
                            .doOnNext(percent -> System.out.println("Batterinivå: " + percent + "%"))
                            .block();
                    break;
                case "6":
                    chargingService.dischargeBattery().block();
                    break;
                case "8":
                    chargingService.stopCharging()
                            .doOnNext(response -> System.out.println("Laddning stoppad"))
                            .block();
                    break;
                case "9":
                    chargingService.stopSmartCharging();
                    System.out.println("Smart laddning stoppad");
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Ogiltigt val. Försök igen");

            }
        }
    }
}