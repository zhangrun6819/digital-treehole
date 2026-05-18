package com.compe.treehole.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "treehole")
public record TreeholeProperties(
        Auth auth,
        Ai ai,
        Storage storage,
        Cleanup cleanup
) {
    public record Auth(String jwtSecret, int tokenDays, int refreshThresholdHours) {
    }

    public record Ai(
            int timeoutSeconds,
            int maxHistoryMessages,
            String provider,
            String apiKey,
            String baseUrl,
            String model
    ) {
    }

    public record Storage(String doodleDir, String publicBaseUrl) {
    }

    public record Cleanup(int orphanDoodleMinutes) {
    }
}
