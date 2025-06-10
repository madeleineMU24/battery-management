package com.example.battery_management;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MenuRunner implements CommandLineRunner {
    private final ChargingService chargingService;

    public MenuRunner(ChargingService chargingService) {
        this.chargingService = chargingService;
    }

    @Override
    public void run(String... args){
        TerminalMenu menu = new TerminalMenu(chargingService);
        menu.show();
    }
}
