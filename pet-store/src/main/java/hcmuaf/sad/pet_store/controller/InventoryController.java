package hcmuaf.sad.pet_store.controller;

import hcmuaf.sad.pet_store.model.InventoryModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/inventory")
public class InventoryController {

    private final InventoryModel inventoryModel;

    public InventoryController(InventoryModel inventoryModel) {
        this.inventoryModel = inventoryModel;
    }

    @GetMapping
    public String inventoryMenu() {
        return "admin/inventory/index";
    }

    @GetMapping("/import")
    public String importPage() {
        return "admin/inventory/import";
    }

    @PostMapping("/import")
    public String importProduct(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam BigDecimal price,
            @RequestParam int quantity
    ) {
        inventoryModel.importProduct(name, description, price, quantity);
        return "redirect:/admin/inventory/import?success";
    }

    @GetMapping("/alerts")
    public String stockAlerts(Model model) {
        model.addAttribute("products", inventoryModel.findLowStockProducts());
        return "admin/inventory/alerts";
    }

    @GetMapping("/adjust")
    public String adjustPage(Model model) {
        model.addAttribute("products", inventoryModel.findAllProducts());
        return "admin/inventory/adjust";
    }

    @GetMapping("/adjust/{id}")
    public String editStock(@PathVariable Long id, Model model) {
        model.addAttribute("product", inventoryModel.findProduct(id));
        return "admin/inventory/edit-stock";
    }

    @PostMapping("/adjust/{id}")
    public String updateStock(
            @PathVariable Long id,
            @RequestParam int quantity
    ) {
        inventoryModel.updateQuantity(id, quantity);
        return "redirect:/admin/inventory/adjust";
    }

    @GetMapping("/thresholds")
    public String thresholdsPage(Model model) {
        model.addAttribute("products", inventoryModel.findAllThresholdProducts());
        return "admin/inventory/thresholds";
    }

    @PostMapping("/thresholds/{id}")
    public String updateThreshold(
            @PathVariable Long id,
            @RequestParam int stockThreshold
    ) {
        inventoryModel.updateStockThreshold(id, stockThreshold);
        return "redirect:/admin/inventory/thresholds";
    }
}