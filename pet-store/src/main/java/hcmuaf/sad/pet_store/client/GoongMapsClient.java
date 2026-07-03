package hcmuaf.sad.pet_store.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Client tích hợp Goong Maps API V2 để tính phí giao hàng.
 *
 * Luồng 2 bước:
 * 1. Geocode địa chỉ text → tọa độ (lat, lng) qua /v2/geocode
 * 2. Tính khoảng cách thực tế (mét) qua /v2/distancematrix
 *
 * Tài liệu: https://docs.goong.io/rest/distance_matrix/
 */
@Slf4j
@Component
public class GoongMapsClient {

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${goong.maps.api-key:}")
    private String apiKey;

    @Value("${goong.maps.base-url:https://rsapi.goong.io}")
    private String baseUrl;

    /**
     * Geocode địa chỉ text thành chuỗi "lat,lng".
     * Trả về null nếu không geocode được.
     */
    private String geocodeAddress(String address) {
        try {
            String url = UriComponentsBuilder.fromUriString(baseUrl + "/v2/geocode")
                    .queryParam("address", address)
                    .queryParam("api_key", apiKey)
                    .build(false)
                    .toUriString();

            JsonNode body = restClient.get().uri(url).retrieve().body(JsonNode.class);
            if (body == null) return null;

            JsonNode results = body.path("results");
            if (!results.isArray() || results.isEmpty()) {
                log.warn("[GoongMaps] Geocode không có kết quả cho: {}", address);
                return null;
            }

            JsonNode location = results.get(0).path("geometry").path("location");
            double lat = location.path("lat").asDouble();
            double lng = location.path("lng").asDouble();

            if (lat == 0.0 && lng == 0.0) {
                log.warn("[GoongMaps] Tọa độ geocode không hợp lệ cho: {}", address);
                return null;
            }

            String coords = lat + "," + lng;
            log.info("[GoongMaps] Geocode \"{}\" → {}", address, coords);
            return coords;

        } catch (RestClientException e) {
            log.error("[GoongMaps] Lỗi geocode \"{}\": {}", address, e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("[GoongMaps] Lỗi xử lý geocode response: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Lấy khoảng cách theo tuyến đường (mét) giữa 2 địa chỉ text.
     * Trả về null nếu có lỗi bất kỳ → kích hoạt Fallback 30,000đ (AC.1).
     */
    public Integer getDistanceInMeters(String originAddress, String destinationAddress) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("[GoongMaps] API Key chưa cấu hình. Dùng phí mặc định (fallback).");
            return null;
        }

        // Bước 1: Geocode cả 2 địa chỉ thành tọa độ
        String originCoords = geocodeAddress(originAddress);
        String destCoords   = geocodeAddress(destinationAddress);

        if (originCoords == null || destCoords == null) {
            log.warn("[GoongMaps] Không geocode được địa chỉ → fallback.");
            return null;
        }

        // Bước 2: Gọi Distance Matrix V2
        try {
            String url = UriComponentsBuilder.fromUriString(baseUrl + "/v2/distancematrix")
                    .queryParam("origins", originCoords)
                    .queryParam("destinations", destCoords)
                    .queryParam("vehicle", "car")
                    .queryParam("api_key", apiKey)
                    .build(false)
                    .toUriString();

            JsonNode body = restClient.get().uri(url).retrieve().body(JsonNode.class);
            if (body == null) {
                log.warn("[GoongMaps] Distance Matrix trả về null.");
                return null;
            }

            JsonNode rows = body.path("rows");
            if (!rows.isArray() || rows.isEmpty()) {
                log.warn("[GoongMaps] Không có rows trong Distance Matrix response.");
                return null;
            }

            JsonNode elements = rows.get(0).path("elements");
            if (!elements.isArray() || elements.isEmpty()) {
                log.warn("[GoongMaps] Không có elements trong Distance Matrix response.");
                return null;
            }

            JsonNode element = elements.get(0);
            String status = element.path("status").asText();
            if (!"OK".equalsIgnoreCase(status)) {
                log.warn("[GoongMaps] Element status không hợp lệ: {}", status);
                return null;
            }

            int distanceMeters = element.path("distance").path("value").asInt();
            log.info("[GoongMaps] Khoảng cách: {} mét", distanceMeters);
            return distanceMeters;

        } catch (RestClientException e) {
            log.error("[GoongMaps] Lỗi gọi Distance Matrix: {}", e.getMessage());
        } catch (Exception e) {
            log.error("[GoongMaps] Lỗi xử lý Distance Matrix response: {}", e.getMessage());
        }

        return null;
    }
}
