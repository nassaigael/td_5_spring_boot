package school.hei.ingredient_api_rest.repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface Repository  <T, ID>{
    T save(T entity) throws SQLException;

    Optional<T> findById(ID id) throws SQLException;

    List<T> findAll() throws SQLException;

    void deleteById(ID id) throws SQLException;

    boolean existsById(ID id) throws SQLException;
}
