package hcmuaf.sad.pet_store.controller;

import hcmuaf.sad.pet_store.dto.request.AddToCartRequest;
import hcmuaf.sad.pet_store.dto.request.UpdateCartRequest;
import hcmuaf.sad.pet_store.dto.response.ApiResponse;
import hcmuaf.sad.pet_store.dto.response.CartResponse;
import hcmuaf.sad.pet_store.model.User;
import hcmuaf.sad.pet_store.service.CartService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // ─── Thymeleaf Page ───────────────────────────────────────────────────────

    @GetMapping("/cart")
    public String cartPage(HttpSession session, Model model) {
        User user = resolveCurrentUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }

        String userCode = user.getUserCode();
        CartResponse cart = cartService.getCart(userCode);
        model.addAttribute("cart", cart);

        // Fetch user's saved addresses to show in the cart
        var addresses = hcmuaf.sad.pet_store.model.ShippingAddress.findAllByUserCode(userCode);
        model.addAttribute("addresses", hcmuaf.sad.pet_store.mapper.AddressMapper.toDtoList(addresses));
        return "cart/cart";
    }

    // ─── REST API (used by JS fetch calls from the page) ──────────────────────

    @ResponseBody
    @GetMapping("/api/v1/cart")
    public ApiResponse<CartResponse> getCart(HttpSession session) {
        User user = resolveCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("UNAUTHORIZED", "Vui long dang nhap.");
        }
        return ApiResponse.success(cartService.getCart(user.getUserCode()));
    }

    @ResponseBody
    @PostMapping("/api/v1/cart/items")
    public ApiResponse<CartResponse> addToCart(HttpSession session,
                                               @Valid @RequestBody AddToCartRequest request) {
        User user = resolveCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("UNAUTHORIZED", "Vui long dang nhap.");
        }
        return ApiResponse.success(cartService.addToCart(user.getUserCode(), request));
    }

    @ResponseBody
    @PutMapping("/api/v1/cart/items/{cartItemId}")
    public ApiResponse<CartResponse> updateCartItem(HttpSession session,
                                                    @PathVariable Long cartItemId,
                                                    @Valid @RequestBody UpdateCartRequest request) {
        User user = resolveCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("UNAUTHORIZED", "Vui long dang nhap.");
        }
        return ApiResponse.success(cartService.updateCartItem(user.getUserCode(), cartItemId, request));
    }

    @ResponseBody
    @DeleteMapping("/api/v1/cart/items/{cartItemId}")
    public ApiResponse<CartResponse> removeCartItem(HttpSession session,
                                                    @PathVariable Long cartItemId) {
        User user = resolveCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("UNAUTHORIZED", "Vui long dang nhap.");
        }
        return ApiResponse.success(cartService.removeCartItem(user.getUserCode(), cartItemId));
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private User resolveCurrentUser(HttpSession session) {
        String userCode = (String) session.getAttribute("userCode");
        if (userCode == null || userCode.isBlank()) {
            return null;
        }
        return User.findActiveByUserCode(userCode);
    }
}
