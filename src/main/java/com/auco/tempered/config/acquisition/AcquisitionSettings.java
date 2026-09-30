package com.auco.tempered.config.acquisition;

public record AcquisitionSettings(AspectLootSettings reinforced, AspectLootSettings swift) {
    public static final AcquisitionSettings DEFAULT = new AcquisitionSettings(
            AspectLootSettings.DEFAULT, AspectLootSettings.DEFAULT);
}
