package com.ds.utils;

public class ParkBean {

    /**
     * plate_number : 湘A888888
     * dev_type : dedicatedLock
     * dev_sn : 54203112230
     */

    private String plate_number;
    private String dev_type;
    private String dev_sn;
    private int parkWaitTime;
    private int parkingWaitSeconds;
    private int outboundCheckSeconds;
    private int outboundWaitSeconds;
    private float triggerDistance;

    public int getParkingWaitSeconds() {
        return parkingWaitSeconds;
    }

    public void setParkingWaitSeconds(int parkingWaitSeconds) {
        this.parkingWaitSeconds = parkingWaitSeconds;
    }

    public int getOutboundCheckSeconds() {
        return outboundCheckSeconds;
    }

    public void setOutboundCheckSeconds(int outboundCheckSeconds) {
        this.outboundCheckSeconds = outboundCheckSeconds;
    }

    public int getOutboundWaitSeconds() {
        return outboundWaitSeconds;
    }

    public void setOutboundWaitSeconds(int outboundWaitSeconds) {
        this.outboundWaitSeconds = outboundWaitSeconds;
    }

    public float getTriggerDistance() {
        return triggerDistance;
    }

    public void setTriggerDistance(float triggerDistance) {
        this.triggerDistance = triggerDistance;
    }

    public String getPlate_number() {
        return plate_number;
    }

    public void setPlate_number(String plate_number) {
        this.plate_number = plate_number;
    }

    public String getDev_type() {
        return dev_type;
    }

    public void setDev_type(String dev_type) {
        this.dev_type = dev_type;
    }

    public String getDev_sn() {
        return dev_sn;
    }

    public void setDev_sn(String dev_sn) {
        this.dev_sn = dev_sn;
    }

    public int getParkWaitTime() {
        return parkWaitTime;
    }

    public void setParkWaitTime(int parkWaitTime) {
        this.parkWaitTime = parkWaitTime;
    }
}
