package orm;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import config.DatabaseConnection;

public abstract class AbstractRepository<T, ID> implements GenericRepository<T, ID> {
    private final Class<T> entityClass;
    protected final Connection connection;

    public AbstractRepository(Connection connection, Class<T> entityClass) {
        this.connection = connection;
        this.entityClass = entityClass;
    }

    @Override
    public void save(T entity) {
        Class<?> clazz = entity.getClass();
        String tableName = clazz.getSimpleName().toLowerCase();

        List<String> columnNames = new ArrayList<>();
        List<String> placeholders = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        // Percorre a hierarquia de herança até chegar em Object
        for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                field.setAccessible(true);

                // Ignora listas, mapas, e objetos complexos (relacionamentos)
                if (Collection.class.isAssignableFrom(field.getType()) ||
                    Map.class.isAssignableFrom(field.getType()) ||
                    (!field.getType().isPrimitive()
                        && !field.getType().getName().startsWith("java.lang")
                        && !field.getType().getName().startsWith("java.time")
                        && !Number.class.isAssignableFrom(field.getType())
                        && !field.getType().equals(Boolean.class))) {
                    continue;
                }

                columnNames.add(field.getName());
                try {
                    values.add(field.get(entity));
                    placeholders.add("?");
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Erro ao acessar valor do campo: " + field.getName(), e);
                }
            }
        }

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
                tableName,
                String.join(", ", columnNames),
                String.join(", ", placeholders)
        );

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar entidade", e);
        }
    }

    @Override
    public T findById(ID id) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                T entity = entityClass.getDeclaredConstructor().newInstance();
                for (Field field : entityClass.getDeclaredFields()) {
                    field.setAccessible(true);
                    field.set(entity, rs.getObject(field.getName()));
                }
                return entity;
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar entidade por ID", e);
        }
        return null;
    }

    private String getTableName() {
        return entityClass.getSimpleName().toLowerCase();
    }
}