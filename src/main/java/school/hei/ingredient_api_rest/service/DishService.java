package school.hei.ingredient_api_rest.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.ingredient_api_rest.model.Dish;
import school.hei.ingredient_api_rest.model.Ingredient;
import school.hei.ingredient_api_rest.repository.DishIngredientRepository;
import school.hei.ingredient_api_rest.repository.DishRepository;
import school.hei.ingredient_api_rest.repository.IngredientRepository;

import java.sql.SQLException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DishService {

    private final DishRepository dishRepository;
    private final IngredientRepository ingredientRepository;
    private final DishIngredientRepository dishIngredientRepository;

    @Transactional
    public Dish createDish(Dish dish) throws SQLException {
        Dish savedDish = dishRepository.save(dish);

        if (dish.getIngredients() != null && !dish.getIngredients().isEmpty()) {
            dishIngredientRepository.updateDishIngredients(savedDish.getId(), dish.getIngredients());
            savedDish.setIngredients(dish.getIngredients());
        }

        return savedDish;
    }

    @Transactional
    public Dish updateDish(Integer id, Dish dish) throws SQLException {
        if (!dishRepository.existsById(id))
            throw new RuntimeException("Dish with id " + id + " not found");

        dish.setId(id);
        Dish updatedDish = dishRepository.save(dish);

        if (dish.getIngredients() != null) {
            dishIngredientRepository.updateDishIngredients(id, dish.getIngredients());
            updatedDish.setIngredients(dish.getIngredients());
        }

        return updatedDish;
    }

    public Dish getDishById(Integer id) throws SQLException {
        return dishRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dish with id " + id + " not found"));
    }

    public List<Dish> getDishesByNameContaining(String namePattern) throws SQLException {
        return dishRepository.findByNameContaining(namePattern);
    }

    public List<Dish> getDishesByIngredientName(String ingredientName) throws SQLException {
        return dishRepository.findByIngredientName(ingredientName);
    }

    public List<Dish> getAllDishes() throws SQLException {
        return dishRepository.findAll();
    }

    public List<Dish> getDishesPaginated(int page, int size) throws SQLException {
        if (page < 1) page = 1;
        if (size < 1) size = 10;
        return dishRepository.findAll(page, size);
    }

    @Transactional
    public void deleteDish(Integer id) throws SQLException {
        if (!dishRepository.existsById(id))
            throw new RuntimeException("Dish with id " + id + " not found");

        dishRepository.deleteById(id);
    }

    @Transactional
    public Dish updateDishIngredients(Integer dishId, List<Ingredient> ingredients) throws SQLException {
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new RuntimeException("Dish with id " + dishId + " not found"));

        List<Ingredient> existingIngredients = ingredients.stream()
                .filter(ingredient -> {
                    try {
                        return ingredientRepository.existsById(ingredient.getId());
                    } catch (SQLException e) {
                        return false;
                    }
                })
                .toList();

        dishIngredientRepository.updateDishIngredients(dishId, existingIngredients);

        dish.setIngredients(existingIngredients);
        return dish;
    }

    public double calculateDishCost(int dishId) throws SQLException {

        Dish dish = getDishById(dishId);
        if (dish.getIngredients() == null || dish.getIngredients().isEmpty())
            return 0.0;

        final int quantities = dishIngredientRepository.getQuantitiesByDishId(dishId).size();

        double totalCost = 0.0;
        for (Ingredient ingredient : dish.getIngredients()) {
            totalCost += ingredient.getPrice() * (double) quantities;
        }

        return totalCost;
    }

    public Double calculateGrossMargin(int dishId) throws SQLException {
        Dish dish = getDishById(dishId);

        if (dish.getSellingPrice() == null)
            throw new RuntimeException("Selling price is not set for dish id: " + dishId);

        double cost = calculateDishCost(dishId);
        return dish.getSellingPrice() - cost;
    }

    public long countDishes() throws SQLException {
        return dishRepository.count();
    }
}