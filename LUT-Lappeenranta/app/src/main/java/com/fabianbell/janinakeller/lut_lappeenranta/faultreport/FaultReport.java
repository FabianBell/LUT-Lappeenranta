package com.fabianbell.janinakeller.lut_lappeenranta.faultreport;

/**
 * Created by Fabian on 27.11.2017.
 */

public class FaultReport {

    private String brokenParts;
    private String lifetime;
    private String reason;
    private String guarantee;
    private String id;

    public FaultReport (String id, String brokenParts, String lifetime, String reason, String guarantee){
        this.brokenParts = brokenParts;
        this.lifetime = lifetime;
        this.reason = reason;
        this. guarantee = guarantee;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBrokenParts() {
        return brokenParts;
    }

    public String getGuarantee() {
        return guarantee;
    }

    public String getLifetime() {
        return lifetime;
    }

    public String getReason() {
        return reason;
    }

    public void setBrokenParts(String brokenParts) {
        this.brokenParts = brokenParts;
    }

    public void setGuarantee(String guarantee) {
        this.guarantee = guarantee;
    }

    public void setLifetime(String lifetime) {
        this.lifetime = lifetime;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
