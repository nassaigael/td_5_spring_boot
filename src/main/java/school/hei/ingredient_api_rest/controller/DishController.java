package school.hei.ingredient_api_rest.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.hei.ingredient_api_rest.dto.ErrorResponse;
import school.hei.ingredient_api_rest.model.Dish;
import school.hei.ingredient_api_rest.model.Ingredient;
import school.hei.ingredient_api_rest.service.DishService;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dishes")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;
    @GetMapping
    public ResponseEntity<?> getAllDishes() {
        try {
            List<Dish> dishes = dishService.getAllDishes();
            return ResponseEntity.ok(dishes);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Database error: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/ingredients")
    public ResponseEntity<?> updateDishIngredients(
            @PathVariable int id,
            @RequestBody(required = false) List<Ingredient> ingredients) {

        if (ingredients == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Request body with ingredients list is required."));

        try {
            Dish updatedDish = dishService.updateDishIngredients(id, ingredients);

            return ResponseEntity.ok(updatedDish);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Dish.id=" + id + " is not found"));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Database error: " + e.getMessage()));
        }
    }
}