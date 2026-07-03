package hcmuaf.sad.pet_store.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private String categoryName;
    private List<VariantResponse> variants;
}
