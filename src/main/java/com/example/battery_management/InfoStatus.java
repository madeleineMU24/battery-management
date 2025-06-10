package com.example.battery_management;

public class InfoStatus {

private int sim_time_hour;
private int sim_time_min;
private double base_current_load;
private double battery_capacity_kWh;
private boolean ev_battery_charge_start_stopp;

private double ev_batt_max_capacity;
private double ev_batt_capacity_kWh;


    public int getSim_time_hour() {
        return sim_time_hour;
    }

    public void setSim_time_hour(int sim_time_hour) {
        this.sim_time_hour = sim_time_hour;
    }

    public int getSim_time_min() {
        return sim_time_min;
    }

    public void setSim_time_min(int sim_time_min) {
        this.sim_time_min = sim_time_min;
    }

    public double getBase_current_load() {
        return base_current_load;
    }

    public void setBase_current_load(double base_current_load) {
        this.base_current_load = base_current_load;
    }

    public double getBattery_capacity_kWh() {
        return battery_capacity_kWh;
    }

    public void setBattery_capacity_kWh(double battery_capacity_kWh) {
        this.battery_capacity_kWh = battery_capacity_kWh;
    }

    public boolean isEv_battery_charge_start_stopp() {
        return ev_battery_charge_start_stopp;
    }

    public void setEv_battery_charge_start_stopp(boolean ev_battery_charge_start_stopp) {
        this.ev_battery_charge_start_stopp = ev_battery_charge_start_stopp;
    }

    public double getEv_batt_max_capacity() {
        return ev_batt_max_capacity;
    }

    public void setEv_batt_max_capacity(double ev_batt_max_capacity) {
        this.ev_batt_max_capacity = ev_batt_max_capacity;
    }

    public double getEv_batt_capacity_kWh() {
        return ev_batt_capacity_kWh;
    }

    public void setEv_batt_capacity_kWh(double ev_batt_capacity_kWh) {
        this.ev_batt_capacity_kWh = ev_batt_capacity_kWh;
    }
}
