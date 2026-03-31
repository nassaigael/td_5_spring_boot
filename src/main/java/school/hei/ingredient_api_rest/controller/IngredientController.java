package school.hei.ingredient_api_rest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.hei.ingredient_api_rest.dto.ErrorResponse;
import school.hei.ingredient_api_rest.dto.StockResponse;
import school.hei.ingredient_api_rest.model.Ingredient;
import school.hei.ingredient_api_rest.model.enums.Unit;
import school.hei.ingredient_api_rest.service.IngredientService;
import school.hei.ingredient_api_rest.service.StockMovementService;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;
    private final StockMovementService stockMovementService;

    @GetMapping
    public ResponseEntity<?> getAllIngredients() {
        try {
            List<Ingredient> ingredients = ingredientService.getAllIngredients();
            return ResponseEntity.ok(ingredients);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Database error: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getIngredientById(@PathVariable int id) {
        try {
            Ingredient ingredient = ingredientService.getIngredientById(id);
            return ResponseEntity.ok(ingredient);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Ingredient.id=" + id + " is not found"));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Database error: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/stock")
    public ResponseEntity<?> getStockValue(
            @PathVariable int id,
            @RequestParam(required = false) String at,
            @RequestParam(required = false) String unit) {

        if (at == null || unit == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Either mandatory query parameter `at` or `unit` is not provided."));
        }

        try {
            try {
                Unit.valueOf(unit.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Invalid unit. Allowed values: PCS, KG, L"));
            }

            Ingredient ingredient = ingredientService.getIngredientById(id);

            Instant instant;
            try {
                instant = Instant.parse(at);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Invalid date format. Use ISO-8601 format (e.g., 2024-01-06T12:00:00Z)"));
            }

            double stockValue = stockMovementService.getStockValueAt(id, instant);

            StockResponse response = new StockResponse(unit.toUpperCase(), stockValue);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found"))
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Ingredient.id=" + id + " is not found"));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Database error: " + e.getMessage()));
        }
    }
}