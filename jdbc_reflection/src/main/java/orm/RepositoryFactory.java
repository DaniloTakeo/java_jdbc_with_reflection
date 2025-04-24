package orm;

import java.sql.Connection;

public class RepositoryFactory {
    private static RepositoryFactory instance;
    private final Connection connection;

    private RepositoryFactory(Connection connection) {
        this.connection = connection;
    }

    public static synchronized RepositoryFactory getInstance(Connection connection) {
        if (instance == null) {
            instance = new RepositoryFactory(connection);
        }
        return instance;
    }

    public <T, ID> GenericRepository<T, ID> createRepository(Class<T> entityClass) {
        return new AbstractRepository<T, ID>(connection, entityClass);
    }
}