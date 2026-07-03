package hcmuaf.sad.pet_store.controller;

import hcmuaf.sad.pet_store.dto.request.ShippingFeeRequest;
import hcmuaf.sad.pet_store.dto.response.ApiResponse;
import hcmuaf.sad.pet_store.dto.response.ShippingFeeResponse;
import hcmuaf.sad.pet_store.service.ShippingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingService shippingService;

    @PostMapping("/calculate")
    public ApiResponse<ShippingFeeResponse> calculateShippingFee(@Valid @RequestBody ShippingFeeRequest request) {
        ShippingFeeResponse response = shippingService.calculateShippingFee(request);
        return ApiResponse.success(response);
    }
}
