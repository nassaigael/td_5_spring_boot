package school.hei.ingredient_api_rest.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.ingredient_api_rest.model.Ingredient;
import school.hei.ingredient_api_rest.model.enums.Category;
import school.hei.ingredient_api_rest.repository.IngredientRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    @Transactional
    public Ingredient createIngredient(Ingredient ingredient) throws SQLException {
        Optional<Ingredient> existingIngredient = ingredientRepository.findByName(ingredient.getName());
        if (existingIngredient.isPresent()) {
            throw new RuntimeException("Ingredient with name '" + ingredient.getName() + "' already exists");
        }

        return ingredientRepository.save(ingredient);
    }

    @Transactional
    public List<Ingredient> createIngredients(List<Ingredient> ingredients) throws SQLException {

        for (Ingredient ingredient : ingredients) {
            Optional<Ingredient> existing = ingredientRepository.findByName(ingredient.getName());
            if (existing.isPresent()) {
                throw new RuntimeException("Ingredient with name '" + ingredient.getName() + "' already exists");
            }
        }

        for (Ingredient ingredient : ingredients) {
            ingredientRepository.save(ingredient);
        }

        return ingredients;
    }

    @Transactional
    public Ingredient updateIngredient(Integer id, Ingredient ingredient) throws SQLException {
        if (!ingredientRepository.existsById(id)) {
            throw new RuntimeException("Ingredient with id " + id + " not found");
        }

        ingredient.setId(id);
        return ingredientRepository.save(ingredient);
    }

    public Ingredient getIngredientById(Integer id) throws SQLException {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingredient with id " + id + " not found"));
    }

    public Optional<Ingredient> findIngredientByName(String name) throws SQLException {
        return ingredientRepository.findByName(name);
    }

    public List<Ingredient> getIngredientsByNameContaining(String namePattern) throws SQLException {
        return ingredientRepository.findByNameContaining(namePattern);
    }

    public List<Ingredient> getIngredientsByCategory(Category category) throws SQLException {
        return ingredientRepository.findByCategory(category);
    }

    public List<Ingredient> getAllIngredients() throws SQLException {
        return ingredientRepository.findAll();
    }

    public List<Ingredient> getIngredientsPaginated(int page, int size) throws SQLException {
        if (page < 1) page = 1;
        if (size < 1) size = 10;

        return ingredientRepository.findAll(page, size);
    }

    public List<Ingredient> getIngredientsByCriteria(
            String ingredientName,
            Category category,
            String dishName,
            int page,
            int size) throws SQLException {
        if (page < 1) page = 1;
        if (size < 1) size = 10;

        return ingredientRepository.findByCriteria(ingredientName, category, dishName, page, size);
    }

    @Transactional
    public void deleteIngredient(Integer id) throws SQLException {
        if (!ingredientRepository.existsById(id)) {
            throw new RuntimeException("Ingredient with id " + id + " not found");
        }
        ingredientRepository.deleteById(id);
    }

    public long countIngredients() throws SQLException {
        return ingredientRepository.count();
    }
}