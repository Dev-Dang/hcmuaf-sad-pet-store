package hcmuaf.sad.pet_store.controller.address;

import hcmuaf.sad.pet_store.exception.BusinessException;
import hcmuaf.sad.pet_store.exception.ErrorCode;
import hcmuaf.sad.pet_store.exception.SystemException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Component
public class GoongMapProvider implements MapProvider {
    private final RestClient restClient = RestClient.create();
    private final String apiKey;
    private final String baseUrl;
    private final String apiVersion;

    public GoongMapProvider(@Value("${goong.maps.api-key:}") String apiKey,
                            @Value("${goong.maps.base-url:}") String baseUrl,
                            @Value("${goong.maps.api-version:}") String apiVersion) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.apiVersion = apiVersion;
    }

    @Override
    public List<MapPrediction> autocomplete(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        // Gọi Goong lấy gợi ý địa chỉ.
        JsonNode root = getJson(uri("/Place/AutoComplete")
                .queryParam("input", keyword)
                .build()
                .toUri());

        List<MapPrediction> result = new ArrayList<>();
        for (JsonNode node : root.path("predictions")) {
            String description = node.path("description").asString(null);
            String placeId = node.path("place_id").asString(null);
            if (description != null && placeId != null) {
                result.add(new MapPrediction(description, placeId));
            }
        }
        return result;
    }

    @Override
    public MapPlaceResult placeDetails(String placeId) {
        if (placeId == null || placeId.isBlank()) {
            throw new BusinessException(ErrorCode.ADDRESS_COORDS_MISSING);
        }

        // Lấy địa chỉ chuẩn và tọa độ từ placeId.
        JsonNode result = getJson(uri("/Place/Detail")
                .queryParam("place_id", placeId)
                .build()
                .toUri()).path("result");

        if (result.isMissingNode()) {
            throw new BusinessException(ErrorCode.ADDRESS_COORDS_MISSING);
        }
        return toPlaceResult(result, placeId);
    }

    @Override
    public MapPlaceResult reverseGeocode(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }

        // Đổi tọa độ thành địa chỉ gần nhất.
        JsonNode results = getJson(uri("/Geocode")
                .queryParam("latlng", latitude + "," + longitude)
                .build()
                .toUri()).path("results");

        return results.isEmpty() ? null : toPlaceResult(results.get(0), null);
    }

    private UriComponentsBuilder uri(String path) {
        return UriComponentsBuilder.fromUriString(baseUrl() + apiPrefix() + path)
                .queryParam("api_key", apiKey());
    }

    private JsonNode getJson(URI uri) {
        try {
            JsonNode root = restClient.get().uri(uri).retrieve().body(JsonNode.class);
            if (root == null) {
                throw new SystemException(ErrorCode.GOONG_MAPS_ERROR);
            }
            return root;
        } catch (RestClientException e) {
            throw new SystemException(ErrorCode.GOONG_MAPS_ERROR, e);
        }
    }

    private MapPlaceResult toPlaceResult(JsonNode node, String fallbackPlaceId) {
        JsonNode location = node.path("geometry").path("location");
        String placeId = node.path("place_id").asText(fallbackPlaceId);
        String address = node.path("formatted_address").asText(null);

        if (placeId == null || address == null || address.isBlank()
                || location.path("lat").isMissingNode()
                || location.path("lng").isMissingNode()) {
            throw new BusinessException(ErrorCode.ADDRESS_COORDS_MISSING);
        }

        return new MapPlaceResult(
                placeId,
                address,
                BigDecimal.valueOf(location.path("lat").asDouble()),
                BigDecimal.valueOf(location.path("lng").asDouble()));
    }

    private String apiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new SystemException(ErrorCode.GOONG_MAPS_ERROR);
        }
        return apiKey;
    }

    private String baseUrl() {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new SystemException(ErrorCode.GOONG_MAPS_ERROR);
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private String apiPrefix() {
        String version = apiVersion;
        if (version == null || version.isBlank()) {
            return "";
        }
        return version.startsWith("/") ? version : "/" + version;
    }
}
