package com.example.server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StatusPayload {
    private String printerId;
    private String status;
    private String cords;
    private Double tempPrintbed;
    private Double tempNozzle;

    // Default constructor
    public StatusPayload() {
    }

    @JsonCreator
    public StatusPayload(@JsonProperty("printerId") String printerId,
                         @JsonProperty("status") String status,
                         @JsonProperty("cords") String cords,
                         @JsonProperty("tempPrintbed") Double tempPrintbed,
                         @JsonProperty("tempNozzle") Double tempNozzle) {
        this.printerId = printerId;
        this.status = status;
        this.cords = cords;
        this.tempPrintbed = tempPrintbed;
        this.tempNozzle = tempNozzle;
    }

    // Getters and setters
    public String getPrinterId() {
        return printerId;
    }

    public void setPrinterId(String printerId) {
        this.printerId = printerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCords() {
        return cords;
    }

    public void setCords(String cords) {
        this.cords = cords;
    }

    public Double getTempPrintbed() {
        return tempPrintbed;
    }

    public void setTempPrintbed(Double tempPrintbed) {
        this.tempPrintbed = tempPrintbed;
    }

    public Double getTempNozzle() {
        return tempNozzle;
    }

    public void setTempNozzle(Double tempNozzle) {
        this.tempNozzle = tempNozzle;
    }

    @Override
    public String toString() {
        return "StatusPayload{" +
                "printerId='" + printerId + '\'' +
                ", status='" + status + '\'' +
                ", cords='" + cords + '\'' +
                ", tempPrintbed=" + tempPrintbed +
                ", tempNozzle=" + tempNozzle +
                '}';
    }
}
