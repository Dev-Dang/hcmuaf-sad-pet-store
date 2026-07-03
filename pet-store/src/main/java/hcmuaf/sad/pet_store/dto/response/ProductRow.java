package hcmuaf.sad.pet_store.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRow {

    private Long id;

    private String name;

    private BigDecimal price;

    private Integer quantity;

}