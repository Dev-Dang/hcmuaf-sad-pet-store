package hcmuaf.sad.pet_store.service;

import hcmuaf.sad.pet_store.dto.request.AddToCartRequest;
import hcmuaf.sad.pet_store.dto.request.UpdateCartRequest;
import hcmuaf.sad.pet_store.dto.response.CartResponse;

public interface CartService {
    CartResponse getCart(Long userId);
    CartResponse addToCart(Long userId, AddToCartRequest request);
    CartResponse updateCartItem(Long userId, Long cartItemId, UpdateCartRequest request);
    CartResponse removeCartItem(Long userId, Long cartItemId);
}
