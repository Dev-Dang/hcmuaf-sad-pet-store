package hcmuaf.sad.pet_store.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GoogleOAuthConfig {
    private static String clientId;

    @Value("${google.client-id}")
    public void setClientId(String id) {
        GoogleOAuthConfig.clientId = id;
    }

    public static String getClientId() {
        return clientId;
    }
}
