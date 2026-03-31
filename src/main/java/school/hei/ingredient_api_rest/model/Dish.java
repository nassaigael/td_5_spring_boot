package school.hei.ingredient_api_rest.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import school.hei.ingredient_api_rest.model.enums.Category;
import school.hei.ingredient_api_rest.model.enums.DishType;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Dish {
    private int id;
    private String name;
    private DishType dishType;
    private Double sellingPrice;
    private List<Ingredient> ingredients;
}
