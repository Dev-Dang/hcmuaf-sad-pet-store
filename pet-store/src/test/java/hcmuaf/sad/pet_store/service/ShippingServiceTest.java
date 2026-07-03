package hcmuaf.sad.pet_store.service;

import hcmuaf.sad.pet_store.dto.request.ShippingFeeRequest;
import hcmuaf.sad.pet_store.dto.response.ShippingFeeResponse;
import hcmuaf.sad.pet_store.service.impl.ShippingServiceImpl;
import hcmuaf.sad.pet_store.client.GoogleMapsClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class ShippingServiceTest {

    @Mock
    private GoogleMapsClient googleMapsClient;

    @InjectMocks
    private ShippingServiceImpl shippingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(shippingService, "storeAddress", "Store Address");
    }

    @Test
    void testFallbackFeeWhenApiReturnsNull() {
        when(googleMapsClient.getDistanceInMeters(anyString(), anyString())).thenReturn(null);

        ShippingFeeRequest req = new ShippingFeeRequest();
        req.setAddressDetail("123");
        req.setWard("Ward");
        req.setDistrict("Dist");
        req.setCity("City");

        ShippingFeeResponse res = shippingService.calculateShippingFee(req);

        assertTrue(res.isFallback());
        assertEquals(new BigDecimal("30000"), res.getFee());
        assertNull(res.getDistanceKm());
    }

    @Test
    void testCalculateFeeLessThan3Km() {
        when(googleMapsClient.getDistanceInMeters(anyString(), anyString())).thenReturn(2500); // 2.5km -> 3km

        ShippingFeeRequest req = new ShippingFeeRequest();
        req.setAddressDetail("123"); req.setWard("Ward"); req.setDistrict("Dist"); req.setCity("City");

        ShippingFeeResponse res = shippingService.calculateShippingFee(req);

        assertFalse(res.isFallback());
        assertEquals(3, res.getDistanceKm());
        assertEquals(new BigDecimal("22000"), res.getFee());
    }

    @Test
    void testCalculateFeeMoreThan3Km() {
        // 4.1km -> ceil to 5km
        // Base: 22000 for 3km. Extra: 2km * 4000 = 8000. Total = 30000
        when(googleMapsClient.getDistanceInMeters(anyString(), anyString())).thenReturn(4100);

        ShippingFeeRequest req = new ShippingFeeRequest();
        req.setAddressDetail("123"); req.setWard("Ward"); req.setDistrict("Dist"); req.setCity("City");

        ShippingFeeResponse res = shippingService.calculateShippingFee(req);

        assertFalse(res.isFallback());
        assertEquals(5, res.getDistanceKm());
        assertEquals(new BigDecimal("30000"), res.getFee());
    }
}
