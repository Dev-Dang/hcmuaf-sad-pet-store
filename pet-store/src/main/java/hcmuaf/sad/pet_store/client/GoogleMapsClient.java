package hcmuaf.sad.pet_store.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleMapsClient {

    private final RestTemplate restTemplate;

    @Value("${app.google.maps.api-key}")
    private String apiKey;

    private static final String DISTANCE_MATRIX_URL = "https://maps.googleapis.com/maps/api/distancematrix/json";

    /**
     * Gets the route distance between two addresses in meters.
     * Returns null if API call fails, times out, or returns invalid status,
     * which will trigger the fallback logic in the service.
     */
    public Integer getDistanceInMeters(String origin, String destination) {
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("DUMMY_API_KEY")) {
            log.warn("Google Maps API Key is missing or dummy. Falling back to default shipping fee.");
            return null; // Triggers AC.1 fallback
        }

        try {
            String url = UriComponentsBuilder.fromUriString(DISTANCE_MATRIX_URL)
                    .queryParam("origins", origin)
                    .queryParam("destinations", destination)
                    .queryParam("key", apiKey)
                    .toUriString();

            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            JsonNode body = response.getBody();

            if (body != null && "OK".equals(body.path("status").asText())) {
                JsonNode rows = body.path("rows");
                if (rows.isArray() && rows.size() > 0) {
                    JsonNode elements = rows.get(0).path("elements");
                    if (elements.isArray() && elements.size() > 0) {
                        JsonNode element = elements.get(0);
                        if ("OK".equals(element.path("status").asText())) {
                            int distanceMeters = element.path("distance").path("value").asInt();
                            log.info("Google Maps Distance: {} meters", distanceMeters);
                            return distanceMeters;
                        } else {
                            log.warn("Google Maps element status: {}", element.path("status").asText());
                        }
                    }
                }
            } else {
                log.warn("Google Maps API status: {}", body != null ? body.path("status").asText() : "null");
            }
        } catch (RestClientException e) {
            log.error("Error calling Google Maps API: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error parsing Google Maps response: {}", e.getMessage());
        }

        return null; // Return null to trigger fallback
    }
}
