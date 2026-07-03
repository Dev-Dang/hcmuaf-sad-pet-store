package hcmuaf.sad.pet_store.service.impl;

import hcmuaf.sad.pet_store.dto.request.AddToCartRequest;
import hcmuaf.sad.pet_store.dto.request.UpdateCartRequest;
import hcmuaf.sad.pet_store.dto.response.CartItemResponse;
import hcmuaf.sad.pet_store.dto.response.CartResponse;
import hcmuaf.sad.pet_store.entity.CartItem;
import hcmuaf.sad.pet_store.entity.ProductVariant;
import hcmuaf.sad.pet_store.entity.User;
import hcmuaf.sad.pet_store.exception.business.InsufficientStockException;
import hcmuaf.sad.pet_store.exception.resource.ResourceNotFoundException;
import hcmuaf.sad.pet_store.mapper.CartMapper;
import hcmuaf.sad.pet_store.repository.CartItemRepository;
import hcmuaf.sad.pet_store.repository.ProductVariantRepository;
import hcmuaf.sad.pet_store.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
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
    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        List<CartItem> items = cartItemRepository.findAllByUserIdAndDeletedAtIsNull(userId);
        return buildCartResponse(items);
    }

    @Override
    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        ProductVariant variant = variantRepository.findByIdAndStatusAndDeletedAtIsNull(request.getVariantId(), ProductVariant.Status.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", request.getVariantId()));

        if (variant.getPrice().compareTo(BigDecimal.ZERO) <= 0 || !variant.getProduct().getStatus().name().equals("ACTIVE")) {
            throw new ResourceNotFoundException("ProductVariant", request.getVariantId()); // Not sellable
        }

        Optional<CartItem> existingItemOpt = cartItemRepository.findByUserIdAndVariantIdAndDeletedAtIsNull(userId, request.getVariantId());

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
            cartItem.setUser(entityManager.getReference(User.class, userId));
            cartItem.setVariant(variant);
            cartItem.setQuantity(request.getQuantity());
        }

        cartItemRepository.save(cartItem);
        return getCart(userId);
    }

    @Override
    public CartResponse updateCartItem(Long userId, Long cartItemId, UpdateCartRequest request) {
        CartItem cartItem = cartItemRepository.findByIdAndUserIdAndDeletedAtIsNull(cartItemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", cartItemId));

        ProductVariant variant = cartItem.getVariant();
        if (request.getQuantity() > variant.getAvailableStock()) {
            throw new InsufficientStockException(variant.getName(), request.getQuantity(), variant.getAvailableStock());
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);
        return getCart(userId);
    }

    @Override
    public CartResponse removeCartItem(Long userId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findByIdAndUserIdAndDeletedAtIsNull(cartItemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", cartItemId));
        
        cartItemRepository.delete(cartItem);
        return getCart(userId);
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
