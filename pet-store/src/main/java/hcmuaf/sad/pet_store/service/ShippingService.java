package hcmuaf.sad.pet_store.service;

import hcmuaf.sad.pet_store.dto.request.ShippingFeeRequest;
import hcmuaf.sad.pet_store.dto.response.ShippingFeeResponse;

public interface ShippingService {
    ShippingFeeResponse calculateShippingFee(ShippingFeeRequest request);
}
