package school.hei.ingredient_api_rest.repository;

import lombok.RequiredArgsConstructor;
import school.hei.ingredient_api_rest.model.Ingredient;
import school.hei.ingredient_api_rest.model.enums.Unit;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@org.springframework.stereotype.Repository
@RequiredArgsConstructor
public class DishIngredientRepository {
    private final DataSource dataSource;
    private final IngredientRepository ingredientRepository;

    public void saveAssociation(int dishId, int ingredientId, double quantity, Unit unit) throws SQLException {
        String sql = """
            INSERT INTO dish_ingredient (id_dish, id_ingredient, required_quantity, unit)
            VALUES (?, ?, ?, ?)
            ON CONFLICT (id_ingredient, id_dish) DO UPDATE SET
                required_quantity = EXCLUDED.required_quantity,
                unit = EXCLUDED.unit
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, dishId);
            pstmt.setInt(2, ingredientId);
            pstmt.setDouble(3, quantity);
            pstmt.setString(4, unit.name());
            pstmt.executeUpdate();
        }
    }

    public void updateDishIngredients(int dishId, List<Ingredient> ingredients) throws SQLException {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            // Supprimer les anciennes associations
            deleteByDishId(dishId, conn);

            // Ajouter les nouvelles associations (avec quantité et unité par défaut)
            String insertSql = "INSERT INTO dish_ingredient (id_dish, id_ingredient, required_quantity, unit) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                for (Ingredient ingredient : ingredients) {
                    pstmt.setInt(1, dishId);
                    pstmt.setInt(2, ingredient.getId());
                    pstmt.setDouble(3, 1.0);
                    pstmt.setString(4, "PCS");
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
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
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, dishId);
            ResultSet rs = pstmt.executeQuery();

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
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, dishId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                quantities.put(rs.getInt("id_ingredient"), rs.getDouble("required_quantity"));
            }
        }
        return quantities;
    }

    public void deleteByDishId(int dishId) throws SQLException {
        String sql = "DELETE FROM dish_ingredient WHERE id_dish = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, dishId);
            pstmt.executeUpdate();
        }
    }

    private void deleteByDishId(int dishId, Connection conn) throws SQLException {
        String sql = "DELETE FROM dish_ingredient WHERE id_dish = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, dishId);
            pstmt.executeUpdate();
        }
    }

    public boolean associationExists(int dishId, int ingredientId) throws SQLException {
        String sql = "SELECT 1 FROM dish_ingredient WHERE id_dish = ? AND id_ingredient = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, dishId);
            pstmt.setInt(2, ingredientId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
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