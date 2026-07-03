package hcmuaf.sad.pet_store.service.impl;

import hcmuaf.sad.pet_store.client.GoongMapsClient;
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

    private final GoongMapsClient goongMapsClient;

    @Value("${app.store.address:}")
    private String storeAddress;

    private static final BigDecimal BASE_FEE = new BigDecimal("22000");
    private static final BigDecimal EXTRA_FEE_PER_KM = new BigDecimal("4000");
    private static final BigDecimal FALLBACK_FEE = new BigDecimal("30000");
    private static final int BASE_DISTANCE_KM = 3;

    @Override
    public ShippingFeeResponse calculateShippingFee(ShippingFeeRequest request) {
        // EX.1: Kiểm tra địa chỉ cửa hàng đã được cấu hình (BRULE-40)
        if (storeAddress == null || storeAddress.isBlank()) {
            log.error("[Shipping] Chưa cấu hình địa chỉ cửa hàng. Không thể tính phí giao hàng.");
            throw new IllegalStateException("Hệ thống chưa cấu hình địa chỉ cửa hàng. Không thể tính phí giao hàng.");
        }

        String customerAddress = request.getResolvedFullAddress();

        // Gọi Goong Maps Distance Matrix API
        Integer distanceMeters = goongMapsClient.getDistanceInMeters(storeAddress, customerAddress);

        // AC.1: Fallback nếu Goong Maps trả về null (lỗi, timeout, hết quota, không geocode được)
        if (distanceMeters == null) {
            log.warn("[Shipping] Dùng phí giao hàng mặc định (30,000đ) cho địa chỉ: {}", customerAddress);
            return ShippingFeeResponse.builder()
                    .fee(FALLBACK_FEE)
                    .distanceKm(null)
                    .isFallback(true)
                    .build();
        }

        // Basic Flow: Tính phí theo khoảng cách (BRULE-39, BR-07)
        // Làm tròn lên theo km (BR-07 rule 5)
        int distanceKm = (int) Math.ceil(distanceMeters / 1000.0);

        BigDecimal finalFee = BASE_FEE;
        if (distanceKm > BASE_DISTANCE_KM) {
            int extraKm = distanceKm - BASE_DISTANCE_KM;
            finalFee = BASE_FEE.add(EXTRA_FEE_PER_KM.multiply(new BigDecimal(extraKm)));
        }

        log.info("[Shipping] Khoảng cách {} km → Phí giao hàng: {}đ", distanceKm, finalFee);

        return ShippingFeeResponse.builder()
                .fee(finalFee)
                .distanceKm(distanceKm)
                .isFallback(false)
                .build();
    }
}
