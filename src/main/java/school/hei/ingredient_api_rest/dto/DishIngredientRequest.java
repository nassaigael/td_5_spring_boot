package school.hei.ingredient_api_rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import school.hei.ingredient_api_rest.model.Ingredient;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DishIngredientRequest {
    private List<Ingredient> ingredients;
}
