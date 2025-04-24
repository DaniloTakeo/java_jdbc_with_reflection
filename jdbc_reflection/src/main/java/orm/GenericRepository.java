package orm;

import java.util.List;
import java.util.Optional;

public interface GenericRepository<T, ID> {
    Optional<T> findById(ID id) throws Exception;
    List<T> findAll() throws Exception;
    void save(T entity);
    void update(T entity);
    void deleteById(ID id);
}