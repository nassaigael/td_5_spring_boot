package school.hei.ingredient_api_rest.repository;

import lombok.RequiredArgsConstructor;
import school.hei.ingredient_api_rest.model.Ingredient;
import school.hei.ingredient_api_rest.model.enums.Category;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

@org.springframework.stereotype.Repository
@RequiredArgsConstructor
public class IngredientRepository implements Repository<Ingredient, Integer> {
    private final DataSource dataSource;

    @Override
    public Ingredient save(Ingredient ingredient) throws SQLException {
        if (ingredient.getId() == 0) {
            return insert(ingredient);
        } else {
            return update(ingredient);
        }
    }

    private Ingredient insert(Ingredient ingredient) throws SQLException {
        String sql = "INSERT INTO ingredient (name, price, category, initial_stock) VALUES (?, ?, ?, ?) RETURNING id";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ingredient.getName());
            pstmt.setDouble(2, ingredient.getPrice());
            pstmt.setString(3, ingredient.getCategory().name());
            pstmt.setBigDecimal(4, BigDecimal.ZERO);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                ingredient.setId(rs.getInt("id"));
            }
        }
        return ingredient;
    }

    private Ingredient update(Ingredient ingredient) throws SQLException {
        String sql = "UPDATE ingredient SET name = ?, price = ?, category = ? WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ingredient.getName());
            pstmt.setDouble(2, ingredient.getPrice());
            pstmt.setString(3, ingredient.getCategory().name());
            pstmt.setInt(4, ingredient.getId());

            pstmt.executeUpdate();
        }
        return ingredient;
    }

    @Override
    public Optional<Ingredient> findById(Integer id) throws SQLException {
        String sql = "SELECT id, name, price, category, initial_stock FROM ingredient WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapIngredient(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<Ingredient> findByName(String name) throws SQLException {
        String sql = "SELECT id, name, price, category, initial_stock FROM ingredient WHERE name = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapIngredient(rs));
            }
        }
        return Optional.empty();
    }

    public List<Ingredient> findByNameContaining(String namePattern) throws SQLException {
        List<Ingredient> ingredients = new ArrayList<>();
        String sql = "SELECT id, name, price, category, initial_stock FROM ingredient WHERE name ILIKE ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + namePattern + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ingredients.add(mapIngredient(rs));
            }
        }
        return ingredients;
    }

    public List<Ingredient> findByCategory(Category category) throws SQLException {
        List<Ingredient> ingredients = new ArrayList<>();
        String sql = "SELECT id, name, price, category, initial_stock FROM ingredient WHERE category = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, category.name());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ingredients.add(mapIngredient(rs));
            }
        }
        return ingredients;
    }

    public List<Ingredient> findByCriteria(String ingredientName, Category category, String dishName, int page, int size) throws SQLException {
        List<Ingredient> ingredients = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                    SELECT DISTINCT i.id, i.name, i.price, i.category, i.initial_stock
                    FROM ingredient i
                    LEFT JOIN dish_ingredient di ON i.id = di.id_ingredient
                    LEFT JOIN dish d ON di.id_dish = d.id
                    WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();

        if (ingredientName != null && !ingredientName.trim().isEmpty()) {
            sql.append(" AND i.name ILIKE ?");
            params.add("%" + ingredientName + "%");
        }

        if (category != null) {
            sql.append(" AND i.category = ?");
            params.add(category.name());
        }

        if (dishName != null && !dishName.trim().isEmpty()) {
            sql.append(" AND d.name ILIKE ?");
            params.add("%" + dishName + "%");
        }

        sql.append(" ORDER BY i.id LIMIT ? OFFSET ?");
        params.add(size);
        params.add((page - 1) * size);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ingredients.add(mapIngredient(rs));
            }
        }
        return ingredients;
    }

    @Override
    public List<Ingredient> findAll() throws SQLException {
        List<Ingredient> ingredients = new ArrayList<>();
        String sql = "SELECT id, name, price, category, initial_stock FROM ingredient ORDER BY id";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ingredients.add(mapIngredient(rs));
            }
        }
        return ingredients;
    }

    public List<Ingredient> findAll(int page, int size) throws SQLException {
        List<Ingredient> ingredients = new ArrayList<>();
        String sql = "SELECT id, name, price, category, initial_stock FROM ingredient ORDER BY id LIMIT ? OFFSET ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, size);
            pstmt.setInt(2, (page - 1) * size);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ingredients.add(mapIngredient(rs));
            }
        }
        return ingredients;
    }

    @Override
    public void deleteById(Integer id) throws SQLException {
        String sql = "DELETE FROM ingredient WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    @Override
    public boolean existsById(Integer id) throws SQLException {
        String sql = "SELECT 1 FROM ingredient WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM ingredient";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Ingredient mapIngredient(ResultSet rs) throws SQLException {
        return new Ingredient(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getDouble("price"),
                Category.valueOf(rs.getString("category"))
        );
    }
}