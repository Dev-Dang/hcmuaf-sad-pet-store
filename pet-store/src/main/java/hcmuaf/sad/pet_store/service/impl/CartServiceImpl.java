package hcmuaf.sad.pet_store.service.impl;

import hcmuaf.sad.pet_store.dto.request.AddToCartRequest;
import hcmuaf.sad.pet_store.dto.request.UpdateCartRequest;
import hcmuaf.sad.pet_store.dto.response.CartItemResponse;
import hcmuaf.sad.pet_store.dto.response.CartResponse;
import hcmuaf.sad.pet_store.entity.CartItem;
import hcmuaf.sad.pet_store.entity.ProductVariant;
import hcmuaf.sad.pet_store.exception.InsufficientStockException;
import hcmuaf.sad.pet_store.exception.ResourceNotFoundException;
import hcmuaf.sad.pet_store.mapper.CartMapper;
import hcmuaf.sad.pet_store.repository.CartItemRepository;
import hcmuaf.sad.pet_store.repository.ProductVariantRepository;
import hcmuaf.sad.pet_store.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository variantRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(String userCode) {
        List<CartItem> items = cartItemRepository.findAllByUserCodeAndDeletedAtIsNull(userCode);
        return buildCartResponse(items);
    }

    @Override
    public CartResponse addToCart(String userCode, AddToCartRequest request) {
        ProductVariant variant = variantRepository.findByIdAndStatusAndDeletedAtIsNull(request.getVariantId(), ProductVariant.Status.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", request.getVariantId()));

        if (variant.getPrice().compareTo(BigDecimal.ZERO) <= 0 || !variant.getProduct().getStatus().name().equals("ACTIVE")) {
            throw new ResourceNotFoundException("ProductVariant", request.getVariantId()); // Not sellable
        }

        Optional<CartItem> existingItemOpt = cartItemRepository.findByUserCodeAndVariantIdAndDeletedAtIsNull(userCode, request.getVariantId());

        CartItem cartItem;
        if (existingItemOpt.isPresent()) {
            cartItem = existingItemOpt.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            if (newQuantity > variant.getAvailableStock()) {
                throw new InsufficientStockException(variant.getName(), newQuantity, variant.getAvailableStock());
            }
            cartItem.setQuantity(newQuantity);
        } else {
            if (request.getQuantity() > variant.getAvailableStock()) {
                throw new InsufficientStockException(variant.getName(), request.getQuantity(), variant.getAvailableStock());
            }
            cartItem = new CartItem();
            cartItem.setUserCode(userCode);
            cartItem.setVariant(variant);
            cartItem.setQuantity(request.getQuantity());
        }

        cartItemRepository.save(cartItem);
        return getCart(userCode);
    }

    @Override
    public CartResponse updateCartItem(String userCode, Long cartItemId, UpdateCartRequest request) {
        CartItem cartItem = cartItemRepository.findByIdAndUserCodeAndDeletedAtIsNull(cartItemId, userCode)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", cartItemId));

        ProductVariant variant = cartItem.getVariant();
        if (request.getQuantity() > variant.getAvailableStock()) {
            throw new InsufficientStockException(variant.getName(), request.getQuantity(), variant.getAvailableStock());
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);
        return getCart(userCode);
    }

    @Override
    public CartResponse removeCartItem(String userCode, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findByIdAndUserCodeAndDeletedAtIsNull(cartItemId, userCode)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", cartItemId));
        
        cartItemRepository.delete(cartItem);
        return getCart(userCode);
    }

    private CartResponse buildCartResponse(List<CartItem> items) {
        List<CartItemResponse> itemResponses = items.stream().map(item -> {
            CartItemResponse response = cartMapper.toResponse(item);
            response.setTotalPrice(response.getUnitPrice().multiply(BigDecimal.valueOf(response.getQuantity())));
            return response;
        }).collect(Collectors.toList());

        BigDecimal total = itemResponses.stream()
                .map(CartItemResponse::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CartResponse cartResponse = new CartResponse();
        cartResponse.setItems(itemResponses);
        cartResponse.setTotalCartPrice(total);
        return cartResponse;
    }
}
