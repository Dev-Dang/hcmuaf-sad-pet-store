package hcmuaf.sad.pet_store.controller.address;

import hcmuaf.sad.pet_store.config.GoongMapConfig;
import hcmuaf.sad.pet_store.exception.BusinessException;
import hcmuaf.sad.pet_store.exception.ErrorCode;
import hcmuaf.sad.pet_store.exception.SystemException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class GoongMap {
    private final RestClient restClient = RestClient.create();

    public List<Prediction> autocomplete(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        // Gọi Goong lấy gợi ý địa chỉ.
        JsonNode root = getJson(uri("/Place/AutoComplete")
                .queryParam("input", keyword)
                .build()
                .toUri());

        List<Prediction> result = new ArrayList<>();
        for (JsonNode node : root.path("predictions")) {
            String description = node.path("description").asText(null);
            String placeId = node.path("place_id").asText(null);
            if (description != null && placeId != null) {
                result.add(new Prediction(description, placeId));
            }
        }
        return result;
    }

    public PlaceResult placeDetails(String placeId) {
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

    public PlaceResult reverseGeocode(BigDecimal latitude, BigDecimal longitude) {
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

    private PlaceResult toPlaceResult(JsonNode node, String fallbackPlaceId) {
        JsonNode location = node.path("geometry").path("location");
        String placeId = node.path("place_id").asText(fallbackPlaceId);
        String address = node.path("formatted_address").asText(null);

        if (placeId == null || address == null || address.isBlank()
                || location.path("lat").isMissingNode()
                || location.path("lng").isMissingNode()) {
            throw new BusinessException(ErrorCode.ADDRESS_COORDS_MISSING);
        }

        return new PlaceResult(
                placeId,
                address,
                BigDecimal.valueOf(location.path("lat").asDouble()),
                BigDecimal.valueOf(location.path("lng").asDouble()));
    }

    private String apiKey() {
        String apiKey = GoongMapConfig.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new SystemException(ErrorCode.GOONG_MAPS_ERROR);
        }
        return apiKey;
    }

    private String baseUrl() {
        String baseUrl = GoongMapConfig.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new SystemException(ErrorCode.GOONG_MAPS_ERROR);
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private String apiPrefix() {
        String version = GoongMapConfig.getApiVersion();
        if (version == null || version.isBlank()) {
            return "";
        }
        return version.startsWith("/") ? version : "/" + version;
    }

    public record Prediction(String description, String placeId) {
    }

    public record PlaceResult(String placeId, String fullAddress, BigDecimal latitude, BigDecimal longitude) {
    }
}
