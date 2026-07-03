package hcmuaf.sad.pet_store.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GoongMapConfig {
    private static String apiKey;
    private static String baseUrl;
    private static String apiVersion;

    @Value("${goong.maps.api-key:}")
    public void setApiKey(String key) {
        GoongMapConfig.apiKey = key;
    }

    @Value("${goong.maps.base-url}")
    public void setBaseUrl(String url) {
        GoongMapConfig.baseUrl = url;
    }

    @Value("${goong.maps.api-version:v2}")
    public void setApiVersion(String version) {
        GoongMapConfig.apiVersion = version;
    }

    public static String getApiKey() {
        return apiKey;
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    public static String getApiVersion() {
        return apiVersion;
    }
}
