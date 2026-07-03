package hcmuaf.sad.pet_store.model;

import java.math.BigDecimal;

public  class RevenueStatRow {
    private String label;
    private int totalOrders;
    private BigDecimal totalRevenue;

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
}