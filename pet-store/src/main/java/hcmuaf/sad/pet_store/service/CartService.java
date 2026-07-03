package hcmuaf.sad.pet_store.service;

import hcmuaf.sad.pet_store.dto.request.AddToCartRequest;
import hcmuaf.sad.pet_store.dto.request.UpdateCartRequest;
import hcmuaf.sad.pet_store.dto.response.CartResponse;

public interface CartService {
    CartResponse getCart(String userCode);
    CartResponse addToCart(String userCode, AddToCartRequest request);
    CartResponse updateCartItem(String userCode, Long cartItemId, UpdateCartRequest request);
    CartResponse removeCartItem(String userCode, Long cartItemId);
}
