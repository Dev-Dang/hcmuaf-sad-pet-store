package hcmuaf.sad.pet_store.controller;

import hcmuaf.sad.pet_store.dto.request.AddToCartRequest;
import hcmuaf.sad.pet_store.dto.request.UpdateCartRequest;
import hcmuaf.sad.pet_store.dto.response.ApiResponse;
import hcmuaf.sad.pet_store.dto.response.CartResponse;
import hcmuaf.sad.pet_store.entity.User;
import hcmuaf.sad.pet_store.repository.UserRepository;
import hcmuaf.sad.pet_store.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    // ─── Thymeleaf Page ───────────────────────────────────────────────────────

    @GetMapping("/cart")
    public String cartPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Long userId = resolveUserId(userDetails);
        CartResponse cart = cartService.getCart(userId);
        model.addAttribute("cart", cart);
        return "cart/cart";
    }

    // ─── REST API (used by JS fetch calls from the page) ──────────────────────

    @ResponseBody
    @GetMapping("/api/v1/cart")
    public ApiResponse<CartResponse> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.success(cartService.getCart(resolveUserId(userDetails)));
    }

    @ResponseBody
    @PostMapping("/api/v1/cart/items")
    public ApiResponse<CartResponse> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddToCartRequest request) {
        return ApiResponse.success(cartService.addToCart(resolveUserId(userDetails), request));
    }

    @ResponseBody
    @PutMapping("/api/v1/cart/items/{cartItemId}")
    public ApiResponse<CartResponse> updateCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartRequest request) {
        return ApiResponse.success(cartService.updateCartItem(resolveUserId(userDetails), cartItemId, request));
    }

    @ResponseBody
    @DeleteMapping("/api/v1/cart/items/{cartItemId}")
    public ApiResponse<CartResponse> removeCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cartItemId) {
        return ApiResponse.success(cartService.removeCartItem(resolveUserId(userDetails), cartItemId));
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private Long resolveUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        return user.getId();
    }
}
