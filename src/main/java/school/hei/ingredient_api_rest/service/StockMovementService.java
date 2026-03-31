package school.hei.ingredient_api_rest.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.ingredient_api_rest.model.Ingredient;
import school.hei.ingredient_api_rest.model.StockMovement;
import school.hei.ingredient_api_rest.model.enums.StockMovementType;
import school.hei.ingredient_api_rest.model.enums.Unit;
import school.hei.ingredient_api_rest.repository.IngredientRepository;
import school.hei.ingredient_api_rest.repository.StockMovementRepository;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final IngredientRepository ingredientRepository;

    @Transactional
    public StockMovement addStockMovement(StockMovement movement) throws SQLException {
        if (!ingredientRepository.existsById(movement.getIngredientId()))
            throw new RuntimeException("Ingredient with id " + movement.getIngredientId() + " not found");

        if (movement.getMovementDatetime() == null)
            movement.setMovementDatetime(Instant.now());

        return stockMovementRepository.save(movement);
    }

    @Transactional
    public StockMovement addStockIn(int ingredientId, double quantity, Unit unit) throws SQLException {
        StockMovement movement = new StockMovement();
        movement.setIngredientId(ingredientId);
        movement.setType(StockMovementType.IN);
        movement.setQuantity(quantity);
        movement.setUnit(unit);
        movement.setMovementDatetime(Instant.now());

        return addStockMovement(movement);
    }

    @Transactional
    public StockMovement addStockOut(int ingredientId, double quantity, Unit unit) throws SQLException {
        double currentStock = getCurrentStock(ingredientId);
        if (currentStock < quantity)
            throw new RuntimeException("Insufficient stock. Current stock: " + currentStock + ", requested: " + quantity);


        StockMovement movement = new StockMovement();
        movement.setIngredientId(ingredientId);
        movement.setType(StockMovementType.OUT);
        movement.setQuantity(quantity);
        movement.setUnit(unit);
        movement.setMovementDatetime(Instant.now());

        return addStockMovement(movement);
    }

    public double getCurrentStock(int ingredientId) throws SQLException {
        if (!ingredientRepository.existsById(ingredientId))
            throw new RuntimeException("Ingredient with id " + ingredientId + " not found");
        return stockMovementRepository.getCurrentStock(ingredientId);
    }

    public double getStockValueAt(int ingredientId, Instant at) throws SQLException {
        if (!ingredientRepository.existsById(ingredientId))
            throw new RuntimeException("Ingredient with id " + ingredientId + " not found");

        return stockMovementRepository.getStockValueAt(ingredientId, at);
    }

    public List<StockMovement> getMovementsByIngredient(int ingredientId) throws SQLException {
        if (!ingredientRepository.existsById(ingredientId))
            throw new RuntimeException("Ingredient with id " + ingredientId + " not found");

        return stockMovementRepository.findByIngredientId(ingredientId);
    }

    public StockMovement getMovementById(Integer id) throws SQLException {
        return stockMovementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock movement with id " + id + " not found"));
    }

    public List<StockMovement> getAllMovements() throws SQLException {
        return stockMovementRepository.findAll();
    }

    public List<StockMovement> getMovementsPaginated(int page, int size) throws SQLException {
        if (page < 1) page = 1;
        if (size < 1) size = 10;

        return stockMovementRepository.findAll(page, size);
    }

    @Transactional
    public void deleteMovement(Integer id) throws SQLException {
        if (!stockMovementRepository.existsById(id))
            throw new RuntimeException("Stock movement with id " + id + " not found");
        stockMovementRepository.deleteById(id);
    }

    public double getTotalStockValue() throws SQLException {
        List<Ingredient> ingredients = ingredientRepository.findAll();
        double totalValue = 0.0;

        for (Ingredient ingredient : ingredients) {
            double stock = getCurrentStock(ingredient.getId());
            totalValue += stock * ingredient.getPrice();
        }
        return totalValue;
    }
}