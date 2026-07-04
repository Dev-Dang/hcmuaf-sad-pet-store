package hcmuaf.sad.pet_store.controller.address;

import java.math.BigDecimal;
import java.util.List;

public interface MapProvider {
    List<MapPrediction> autocomplete(String keyword);

    MapPlaceResult placeDetails(String placeId);

    MapPlaceResult reverseGeocode(BigDecimal latitude, BigDecimal longitude);
}
