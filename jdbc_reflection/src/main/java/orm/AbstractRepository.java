package orm;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
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
        Class<?> clazz = entityClass;
        String tableName = clazz.getSimpleName().toLowerCase();

        String sql = String.format("SELECT * FROM %s WHERE id = ?", tableName);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id); // usa setObject para aceitar qualquer tipo de ID
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                T entity = entityClass.getDeclaredConstructor().newInstance();

                for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
                    for (Field field : current.getDeclaredFields()) {
                        field.setAccessible(true);
                        try {
                            Object value = rs.getObject(field.getName());

                            if (value != null) {
                                if (field.getType().isEnum()) {
                                    Object enumValue = Enum.valueOf((Class<Enum>) field.getType(), value.toString());
                                    field.set(entity, enumValue);
                                } else if (field.getType().equals(LocalDate.class) && value instanceof Date) {
                                    field.set(entity, ((Date) value).toLocalDate());
                                } else if (field.getType().equals(BigDecimal.class)) {
                                    field.set(entity, new BigDecimal(value.toString()));
                                } else {
                                    field.set(entity, value);
                                }
                            }
                        } catch (SQLException | IllegalArgumentException e) {
                            System.err.println("Erro ao mapear campo: " + field.getName() + " — " + e.getMessage());
                        }
                    }
                }

                return entity;
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar entidade por ID", e);
        }

        return null;
    }
    
    @Override
    public List<T> findAll() {
        Class<?> clazz = entityClass;
        String tableName = clazz.getSimpleName().toLowerCase();

        String sql = String.format("SELECT * FROM %s", tableName);

        List<T> entities = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                T entity = entityClass.getDeclaredConstructor().newInstance();

                // Itera pelos campos da classe e de suas superclasses
                for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
                    for (Field field : current.getDeclaredFields()) {
                        field.setAccessible(true);
                        try {
                            Object value = rs.getObject(field.getName());

                            if (value != null) {
                                if (field.getType().isEnum()) {
                                    Object enumValue = Enum.valueOf((Class<Enum>) field.getType(), value.toString());
                                    field.set(entity, enumValue);
                                } else if (field.getType().equals(LocalDate.class) && value instanceof Date) {
                                    field.set(entity, ((Date) value).toLocalDate());
                                } else if (field.getType().equals(BigDecimal.class)) {
                                    field.set(entity, new BigDecimal(value.toString()));
                                } else {
                                    field.set(entity, value);
                                }
                            }

                        } catch (SQLException | IllegalArgumentException e) {
                            System.err.println("Erro ao mapear campo: " + field.getName() + " — " + e.getMessage());
                        }
                    }
                }

                entities.add(entity);
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar entidades", e);
        }

        return entities;
    }

    private String getTableName() {
        return entityClass.getSimpleName().toLowerCase();
    }
}