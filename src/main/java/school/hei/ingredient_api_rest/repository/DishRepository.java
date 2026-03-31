package school.hei.ingredient_api_rest.repository;

import lombok.RequiredArgsConstructor;
import school.hei.ingredient_api_rest.model.Dish;
import school.hei.ingredient_api_rest.model.Ingredient;
import school.hei.ingredient_api_rest.model.enums.DishType;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@org.springframework.stereotype.Repository
@RequiredArgsConstructor
public class DishRepository implements Repository<Dish, Integer> {
    private final DataSource dataSource;
    private final DishIngredientRepository dishIngredientRepository;

    @Override
    public Dish save(Dish dish) throws SQLException {
        if (dish.getId() == 0) {
            return insert(dish);
        } else {
            return update(dish);
        }
    }

    private Dish insert(Dish dish) throws SQLException {
        String sql = "INSERT INTO dish (name, dish_type, selling_price) VALUES (?, ?, ?) RETURNING id";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, dish.getName());
            pstmt.setString(2, dish.getDishType().name());
            if (dish.getSellingPrice() != null) {
                pstmt.setDouble(3, dish.getSellingPrice());
            } else {
                pstmt.setNull(3, Types.DOUBLE);
            }

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                dish.setId(rs.getInt("id"));
            }
        }
        return dish;
    }

    private Dish update(Dish dish) throws SQLException {
        String sql = "UPDATE dish SET name = ?, dish_type = ?, selling_price = ? WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, dish.getName());
            pstmt.setString(2, dish.getDishType().name());
            if (dish.getSellingPrice() != null) {
                pstmt.setDouble(3, dish.getSellingPrice());
            } else {
                pstmt.setNull(3, Types.DOUBLE);
            }
            pstmt.setInt(4, dish.getId());

            pstmt.executeUpdate();
        }
        return dish;
    }

    @Override
    public Optional<Dish> findById(Integer id) throws SQLException {
        String sql = "SELECT id, name, dish_type, selling_price FROM dish WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Dish dish = mapDish(rs);
                List<Ingredient> ingredients = dishIngredientRepository.findIngredientsByDishId(id);
                dish.setIngredients(ingredients);
                return Optional.of(dish);
            }
        }
        return Optional.empty();
    }

    public List<Dish> findByNameContaining(String namePattern) throws SQLException {
        List<Dish> dishes = new ArrayList<>();
        String sql = "SELECT id, name, dish_type, selling_price FROM dish WHERE name ILIKE ?";

        return getDishes(namePattern, dishes, sql);
    }

    private List<Dish> getDishes(String namePattern, List<Dish> dishes, String sql) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + namePattern + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Dish dish = mapDish(rs);
                dish.setIngredients(dishIngredientRepository.findIngredientsByDishId(dish.getId()));
                dishes.add(dish);
            }
        }
        return dishes;
    }

    public List<Dish> findByIngredientName(String ingredientName) throws SQLException {
        List<Dish> dishes = new ArrayList<>();
        String sql = """
            SELECT DISTINCT d.id, d.name, d.dish_type, d.selling_price
            FROM dish d
            JOIN dish_ingredient di ON d.id = di.id_dish
            JOIN ingredient i ON di.id_ingredient = i.id
            WHERE i.name ILIKE ?
            ORDER BY d.id
        """;

        return getDishes(ingredientName, dishes, sql);
    }

    @Override
    public List<Dish> findAll() throws SQLException {
        List<Dish> dishes = new ArrayList<>();
        String sql = "SELECT id, name, dish_type, selling_price FROM dish ORDER BY id";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Dish dish = mapDish(rs);
                dish.setIngredients(dishIngredientRepository.findIngredientsByDishId(dish.getId()));
                dishes.add(dish);
            }
        }
        return dishes;
    }

    public List<Dish> findAll(int page, int size) throws SQLException {
        List<Dish> dishes = new ArrayList<>();
        String sql = "SELECT id, name, dish_type, selling_price FROM dish ORDER BY id LIMIT ? OFFSET ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, size);
            pstmt.setInt(2, (page - 1) * size);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Dish dish = mapDish(rs);
                dish.setIngredients(dishIngredientRepository.findIngredientsByDishId(dish.getId()));
                dishes.add(dish);
            }
        }
        return dishes;
    }

    @Override
    public void deleteById(Integer id) throws SQLException {
        // D'abord supprimer les associations dans dish_ingredient
        dishIngredientRepository.deleteByDishId(id);

        // Ensuite supprimer le plat
        String sql = "DELETE FROM dish WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    @Override
    public boolean existsById(Integer id) throws SQLException {
        String sql = "SELECT 1 FROM dish WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM dish";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Dish mapDish(ResultSet rs) throws SQLException {
        String dishTypeStr = rs.getString("dish_type");
        DishType dishType = dishTypeStr != null ? DishType.valueOf(dishTypeStr) : null;
        Double sellingPrice = rs.getDouble("selling_price");
        if (rs.wasNull()) {
            sellingPrice = null;
        }

        return new Dish(
                rs.getInt("id"),
                rs.getString("name"),
                dishType,
                sellingPrice,
                new ArrayList<>()
        );
    }
}