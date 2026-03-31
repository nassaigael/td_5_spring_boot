package school.hei.ingredient_api_rest.repository;

import lombok.RequiredArgsConstructor;
import school.hei.ingredient_api_rest.model.StockMovement;
import school.hei.ingredient_api_rest.model.enums.StockMovementType;
import school.hei.ingredient_api_rest.model.enums.Unit;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.*;

@org.springframework.stereotype.Repository
@RequiredArgsConstructor
public class StockMovementRepository implements Repository<StockMovement, Integer> {
    private final DataSource dataSource;

    @Override
    public StockMovement save(StockMovement movement) throws SQLException {
        if (movement.getId() == 0) {
            return insert(movement);
        } else {
            return update(movement);
        }
    }

    private StockMovement insert(StockMovement movement) throws SQLException {
        String sql = """
            INSERT INTO stock_movement (id_ingredient, quantity, unit, type, creation_datetime)
            VALUES (?, ?, ?, ?, ?) RETURNING id
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, movement.getIngredientId());
            pstmt.setDouble(2, movement.getQuantity());
            pstmt.setString(3, movement.getUnit().name());
            pstmt.setString(4, movement.getType().name());
            pstmt.setTimestamp(5, Timestamp.from(movement.getMovementDatetime()));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                movement.setId(rs.getInt("id"));
            }
        }
        return movement;
    }

    private StockMovement update(StockMovement movement) throws SQLException {
        String sql = """
            UPDATE stock_movement 
            SET id_ingredient = ?, quantity = ?, unit = ?, type = ?, creation_datetime = ?
            WHERE id = ?
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, movement.getIngredientId());
            pstmt.setDouble(2, movement.getQuantity());
            pstmt.setString(3, movement.getUnit().name());
            pstmt.setString(4, movement.getType().name());
            pstmt.setTimestamp(5, Timestamp.from(movement.getMovementDatetime()));
            pstmt.setInt(6, movement.getId());

            pstmt.executeUpdate();
        }
        return movement;
    }

    @Override
    public Optional<StockMovement> findById(Integer id) throws SQLException {
        String sql = "SELECT id, id_ingredient, quantity, unit, type, creation_datetime FROM stock_movement WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapStockMovement(rs));
            }
        }
        return Optional.empty();
    }

    public List<StockMovement> findByIngredientId(int ingredientId) throws SQLException {
        List<StockMovement> movements = new ArrayList<>();
        String sql = "SELECT id, id_ingredient, quantity, unit, type, creation_datetime FROM stock_movement WHERE id_ingredient = ? ORDER BY creation_datetime";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ingredientId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                movements.add(mapStockMovement(rs));
            }
        }
        return movements;
    }

    public double getStockValueAt(int ingredientId, Instant at) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(
                CASE WHEN type = 'IN' THEN quantity ELSE -quantity END
            ), 0) as stock_value
            FROM stock_movement
            WHERE id_ingredient = ? AND creation_datetime <= ?
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ingredientId);
            pstmt.setTimestamp(2, Timestamp.from(at));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("stock_value");
            }
        }
        return 0.0;
    }

    public double getCurrentStock(int ingredientId) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(
                CASE WHEN type = 'IN' THEN quantity ELSE -quantity END
            ), 0) as stock_value
            FROM stock_movement
            WHERE id_ingredient = ?
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ingredientId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("stock_value");
            }
        }
        return 0.0;
    }

    @Override
    public List<StockMovement> findAll() throws SQLException {
        List<StockMovement> movements = new ArrayList<>();
        String sql = "SELECT id, id_ingredient, quantity, unit, type, creation_datetime FROM stock_movement ORDER BY creation_datetime";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                movements.add(mapStockMovement(rs));
            }
        }
        return movements;
    }

    public List<StockMovement> findAll(int page, int size) throws SQLException {
        List<StockMovement> movements = new ArrayList<>();
        String sql = "SELECT id, id_ingredient, quantity, unit, type, creation_datetime FROM stock_movement ORDER BY creation_datetime LIMIT ? OFFSET ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, size);
            pstmt.setInt(2, (page - 1) * size);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                movements.add(mapStockMovement(rs));
            }
        }
        return movements;
    }

    @Override
    public void deleteById(Integer id) throws SQLException {
        String sql = "DELETE FROM stock_movement WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    @Override
    public boolean existsById(Integer id) throws SQLException {
        String sql = "SELECT 1 FROM stock_movement WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    private StockMovement mapStockMovement(ResultSet rs) throws SQLException {
        return new StockMovement(
                rs.getInt("id"),
                rs.getInt("id_ingredient"),
                StockMovementType.valueOf(rs.getString("type")),
                rs.getDouble("quantity"),
                Unit.valueOf(rs.getString("unit")),
                rs.getTimestamp("creation_datetime").toInstant()
        );
    }
}