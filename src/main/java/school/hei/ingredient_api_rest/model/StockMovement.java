package school.hei.ingredient_api_rest.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import school.hei.ingredient_api_rest.model.enums.StockMovementType;
import school.hei.ingredient_api_rest.model.enums.Unit;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StockMovement {
    private int id;
    private int ingredientId;
    private StockMovementType type;
    private double quantity;
    private Unit unit;
    private Instant movementDatetime;
}
