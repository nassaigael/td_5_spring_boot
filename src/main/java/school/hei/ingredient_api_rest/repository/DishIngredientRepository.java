package school.hei.ingredient_api_rest.repository;

import lombok.RequiredArgsConstructor;
import school.hei.ingredient_api_rest.model.Ingredient;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@org.springframework.stereotype.Repository
@RequiredArgsConstructor
public class DishIngredientRepository {
    private final DataSource dataSource;

    public void updateDishIngredients(int dishId, List<Ingredient> ingredients) throws SQLException {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            deleteByDishId(dishId, conn);

            String insertSql = """
                    INSERT INTO dish_ingredient (id_dish, id_ingredient, required_quantity, unit)
                    VALUES (?, ?, ?, CAST(? AS unit))
                    """;
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (Ingredient ingredient : ingredients) {
                    ps.setInt(1, dishId);
                    ps.setInt(2, ingredient.getId());
                    ps.setDouble(3, 1.0);
                    ps.setString(4, "PCS");
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.close();
        }
    }

    public List<Ingredient> findIngredientsByDishId(int dishId) throws SQLException {
        List<Ingredient> ingredients = new ArrayList<>();
        String sql = """
                    SELECT i.id, i.name, i.price, i.category, i.initial_stock
                    FROM ingredient i
                    JOIN dish_ingredient di ON i.id = di.id_ingredient
                    WHERE di.id_dish = ?
                    ORDER BY i.id
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, dishId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ingredients.add(mapIngredient(rs));
            }
        }
        return ingredients;
    }

    public Map<Integer, Double> getQuantitiesByDishId(int dishId) throws SQLException {
        Map<Integer, Double> quantities = new HashMap<>();
        String sql = "SELECT id_ingredient, required_quantity FROM dish_ingredient WHERE id_dish = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, dishId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                quantities.put(rs.getInt("id_ingredient"), rs.getDouble("required_quantity"));
            }
        }
        return quantities;
    }

    public void deleteByDishId(int dishId) throws SQLException {
        String sql = "DELETE FROM dish_ingredient WHERE id_dish = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, dishId);
            ps.executeUpdate();
        }
    }

    private void deleteByDishId(int dishId, Connection conn) throws SQLException {
        String sql = "DELETE FROM dish_ingredient WHERE id_dish = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dishId);
            ps.executeUpdate();
        }
    }

    private Ingredient mapIngredient(ResultSet rs) throws SQLException {
        return new Ingredient(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getDouble("price"),
                school.hei.ingredient_api_rest.model.enums.Category.valueOf(rs.getString("category"))
        );
    }
}