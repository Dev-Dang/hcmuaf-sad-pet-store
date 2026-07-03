package hcmuaf.sad.pet_store.controller;

import hcmuaf.sad.pet_store.model.RevenueModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RevenueController {

    private final RevenueModel revenueModel;

    public RevenueController(RevenueModel revenueModel) {
        this.revenueModel = revenueModel;
    }

    @GetMapping("/admin/revenue")
    public String revenue(Model model) {
        model.addAttribute("summary", revenueModel.getSummary());
        model.addAttribute("orders", revenueModel.findPaidOrders());
        model.addAttribute("dailyRevenue", revenueModel.getDailyRevenue());
        model.addAttribute("monthlyRevenue", revenueModel.getMonthlyRevenue());
        model.addAttribute("yearlyRevenue", revenueModel.getYearlyRevenue());
        return "admin/revenue/index";
    }
}