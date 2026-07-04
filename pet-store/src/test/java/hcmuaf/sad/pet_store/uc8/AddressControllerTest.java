package hcmuaf.sad.pet_store.uc8;

import hcmuaf.sad.pet_store.controller.address.AddressController;
import hcmuaf.sad.pet_store.controller.address.MapPlaceResult;
import hcmuaf.sad.pet_store.controller.address.MapPrediction;
import hcmuaf.sad.pet_store.controller.address.MapProvider;
import hcmuaf.sad.pet_store.exception.BusinessException;
import hcmuaf.sad.pet_store.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class AddressControllerTest {
    private FakeMapProvider mapProvider;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mapProvider = new FakeMapProvider();
        mockMvc = MockMvcBuilders.standaloneSetup(new AddressController(mapProvider))
                .build();
    }

    @Test
    void autocomplete_shouldReturnPredictionsFromMapProvider() throws Exception {
        mapProvider.predictions = List.of(new MapPrediction("Linh Trung, Thu Duc", "place-1"));

        // [8.6.4] Gửi từ khóa lấy gợi ý địa chỉ
        mockMvc.perform(get("/account/addresses/autocomplete")
                        .param("keyword", "Linh Trung"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.predictions[0].description").value("Linh Trung, Thu Duc"))
                .andExpect(jsonPath("$.predictions[0].placeId").value("place-1"));
    }

    @Test
    void placeDetails_shouldReturnPlaceResultFromMapProvider() throws Exception {
        mapProvider.placeResult = new MapPlaceResult(
                "place-1",
                "Linh Trung, Thu Duc",
                new BigDecimal("10.87000000"),
                new BigDecimal("106.80000000"));

        // [8.6.6] Lấy địa chỉ đầy đủ và tọa độ theo mã địa điểm
        mockMvc.perform(get("/account/addresses/place-details")
                        .param("placeId", "place-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.placeId").value("place-1"))
                .andExpect(jsonPath("$.fullAddress").value("Linh Trung, Thu Duc"))
                .andExpect(jsonPath("$.latitude").value(10.87000000))
                .andExpect(jsonPath("$.longitude").value(106.80000000));
    }

    @Test
    void createAddress_placeDetailsBusinessException_shouldReturnFormWithPlaceIdError() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userCode", "KHG-0000001");
        mapProvider.placeDetailsException = new BusinessException(ErrorCode.ADDRESS_COORDS_MISSING);

        mockMvc.perform(post("/account/addresses")
                        .session(session)
                        .param("recipientName", "Nguyen Van A")
                        .param("phone", "0900000000")
                        .param("placeId", "bad-place")
                        .param("fullAddress", "Linh Trung, Thu Duc")
                        .param("addressDetail", "So 1"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/address-form"))
                .andExpect(model().attributeHasFieldErrors("addressRequest", "placeId"));
    }

    private static class FakeMapProvider implements MapProvider {
        private List<MapPrediction> predictions = List.of();
        private MapPlaceResult placeResult;
        private MapPlaceResult reverseGeocodeResult;
        private BusinessException placeDetailsException;

        @Override
        public List<MapPrediction> autocomplete(String keyword) {
            return predictions;
        }

        @Override
        public MapPlaceResult placeDetails(String placeId) {
            if (placeDetailsException != null) {
                throw placeDetailsException;
            }
            return placeResult;
        }

        @Override
        public MapPlaceResult reverseGeocode(BigDecimal latitude, BigDecimal longitude) {
            return reverseGeocodeResult;
        }
    }
}
