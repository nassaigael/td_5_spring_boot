package school.hei.ingredient_api_rest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.hei.ingredient_api_rest.dto.CreateDishRequest;
import school.hei.ingredient_api_rest.dto.ErrorResponse;
import school.hei.ingredient_api_rest.model.Dish;
import school.hei.ingredient_api_rest.model.Ingredient;
import school.hei.ingredient_api_rest.service.DishService;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dishes")
@RequiredArgsConstructor
public class DishController {
    private final DishService dishService;

    @GetMapping
    public ResponseEntity<?> getAllDishes(
            @RequestParam(required = false) Double priceUnder,
            @RequestParam(required = false) Double priceOver,
            @RequestParam(required = false) String name) {

        try {
            List<Dish> dishes;

            if (priceUnder != null || priceOver != null || (name != null && !name.trim().isEmpty()))
                dishes = dishService.getDishesWithFilters(name, priceOver, priceUnder);
            else
                dishes = dishService.getAllDishes();


            return ResponseEntity.ok(dishes);

        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Database error: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createDishes(
            @RequestBody(required = false) List<CreateDishRequest> dishRequests
    ) {
        if (dishRequests == null || dishRequests.isEmpty())
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Request body with list of dishes is required."));

        try {
            List<Dish> dishesToCreate = dishRequests.stream()
                    .map(req -> new Dish(
                            0,
                            req.getName(),
                            req.getDishType(),
                            req.getSellingPrice(),
                            List.of()
                    ))
                    .collect(Collectors.toList());

            List<Dish> createdDishes = dishService.createDishes(dishesToCreate);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdDishes);

        } catch (RuntimeException e) {
            if (e.getMessage().startsWith("Dish.name="))
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse(e.getMessage()));

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Database error: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/ingredients")
    public ResponseEntity<?> updateDishIngredients(
            @PathVariable int id,
            @RequestBody(required = false) List<Ingredient> ingredients) {

        if (ingredients == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Request body with ingredients list is required."));
        }

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