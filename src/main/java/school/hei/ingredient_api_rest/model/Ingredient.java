package school.hei.ingredient_api_rest.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import school.hei.ingredient_api_rest.model.enums.Category;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Ingredient {
    private int id;
    private String name;
    private Double price;
    private Category category;
}
