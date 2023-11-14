package io.lumstudio.yohub.model;

public class Soc {

    private String VENDOR;
    private String NAME;
    private String FAB;
    private String CPU;
    private String MEMORY;
    private String BANDWIDTH;
    private String CHANNELS;

    public Soc() {
    }

    public Soc(String VENDOR, String NAME, String FAB, String CPU, String MEMORY, String BANDWIDTH, String CHANNELS) {
        this.VENDOR = VENDOR;
        this.NAME = NAME;
        this.FAB = FAB;
        this.CPU = CPU;
        this.MEMORY = MEMORY;
        this.BANDWIDTH = BANDWIDTH;
        this.CHANNELS = CHANNELS;
    }

    public String getVENDOR() {
        return VENDOR;
    }

    public void setVENDOR(String vENDOR) {
        VENDOR = vENDOR;
    }

    public String getNAME() {
        return NAME;
    }

    public void setNAME(String nAME) {
        NAME = nAME;
    }

    public String getFAB() {
        return FAB;
    }

    public void setFAB(String fAB) {
        FAB = fAB;
    }

    public String getCPU() {
        return CPU;
    }

    public void setCPU(String cPU) {
        CPU = cPU;
    }

    public String getMEMORY() {
        return MEMORY;
    }

    public void setMEMORY(String mEMORY) {
        MEMORY = mEMORY;
    }

    public String getBANDWIDTH() {
        return BANDWIDTH;
    }

    public void setBANDWIDTH(String bANDWIDTH) {
        BANDWIDTH = bANDWIDTH;
    }

    public String getCHANNELS() {
        return CHANNELS;
    }

    public void setCHANNELS(String cHANNELS) {
        CHANNELS = cHANNELS;
    }

    @Override
    public String toString() {
        return  "Vendor: " + VENDOR + "\n" +
                "FAB: " + FAB + "\n" +
                "CPU: \n" + CPU + "\n"
                + (MEMORY == null || MEMORY.isEmpty() ? "" : "Memory: " + MEMORY)
                +(CHANNELS == null || CHANNELS.isEmpty() ? "" : "\nChannels: " + CHANNELS)
                + (BANDWIDTH == null || BANDWIDTH.isEmpty() ? "" : "\nBandwidth: " + BANDWIDTH);
    }
}
