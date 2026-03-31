package school.hei.ingredient_api_rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import school.hei.ingredient_api_rest.model.enums.DishType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateDishRequest {
    private String name;
    private DishType dishType;
    private Double sellingPrice;
}