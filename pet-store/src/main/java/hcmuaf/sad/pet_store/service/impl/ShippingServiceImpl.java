package hcmuaf.sad.pet_store.service.impl;

import hcmuaf.sad.pet_store.client.GoogleMapsClient;
import hcmuaf.sad.pet_store.dto.request.ShippingFeeRequest;
import hcmuaf.sad.pet_store.dto.response.ShippingFeeResponse;
import hcmuaf.sad.pet_store.service.ShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {

    private final GoogleMapsClient googleMapsClient;

    @Value("${app.store.address:}")
    private String storeAddress;

    private static final BigDecimal BASE_FEE = new BigDecimal("22000");
    private static final BigDecimal EXTRA_FEE_PER_KM = new BigDecimal("4000");
    private static final BigDecimal FALLBACK_FEE = new BigDecimal("30000");
    private static final int BASE_DISTANCE_KM = 3;

    @Override
    public ShippingFeeResponse calculateShippingFee(ShippingFeeRequest request) {
        // EX.1: Check if store address is configured properly
        if (storeAddress == null || storeAddress.isBlank()) {
            log.error("Store address is not configured. Cannot calculate shipping fee.");
            throw new IllegalStateException("Hệ thống chưa cấu hình địa chỉ cửa hàng. Không thể tính phí giao hàng.");
        }

        String customerAddress = request.getFullAddress();

        // Call Google Maps API
        Integer distanceMeters = googleMapsClient.getDistanceInMeters(storeAddress, customerAddress);

        // AC.1: Fallback if Google Maps fails (returns null)
        if (distanceMeters == null) {
            log.warn("Using fallback shipping fee (30,000) for address: {}", customerAddress);
            return ShippingFeeResponse.builder()
                    .fee(FALLBACK_FEE)
                    .distanceKm(null)
                    .isFallback(true)
                    .build();
        }

        // Basic Flow: Calculate fee based on distance
        // Distance is rounded up to the nearest km
        int distanceKm = (int) Math.ceil(distanceMeters / 1000.0);
        
        BigDecimal finalFee = BASE_FEE;
        if (distanceKm > BASE_DISTANCE_KM) {
            int extraKm = distanceKm - BASE_DISTANCE_KM;
            finalFee = BASE_FEE.add(EXTRA_FEE_PER_KM.multiply(new BigDecimal(extraKm)));
        }

        log.info("Calculated fee for {} km: {}", distanceKm, finalFee);
        
        return ShippingFeeResponse.builder()
                .fee(finalFee)
                .distanceKm(distanceKm)
                .isFallback(false)
                .build();
    }
}
