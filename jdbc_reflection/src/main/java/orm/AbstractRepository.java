package orm;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import config.DatabaseConnection;
import orm.annotations.Column;
import orm.annotations.Id;
import orm.annotations.Transient;

public abstract class AbstractRepository<T, ID> implements GenericRepository<T, ID> {
    private final Class<T> entityClass;
    protected final Connection connection;

    public AbstractRepository(Connection connection, Class<T> entityClass) {
        this.connection = connection;
        this.entityClass = entityClass;
    }

    @Override
    public void save(T entity) {
        try {
            Map<String, Object> columnValues = getColumnNamesAndValues(entity);
            
            // Gerar SQL de INSERT
            StringBuilder sql = new StringBuilder("INSERT INTO ");
            sql.append(getTableName()); // Nome da tabela
            sql.append(" (");
            
            // Colocar os nomes das colunas (sem o 'id')
            columnValues.keySet().forEach(col -> sql.append(col).append(", "));
            sql.deleteCharAt(sql.length() - 2);  // Remove a última vírgula
            sql.append(") VALUES (");
            
            // Colocar os valores
            columnValues.values().forEach(val -> sql.append("'").append(val).append("', "));
            sql.deleteCharAt(sql.length() - 2);  // Remove a última vírgula
            sql.append(")");

            // Preparar o statement e executá-lo
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.executeUpdate();
                
                // Recuperar a chave gerada automaticamente (id)
                try (var generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        // Assumindo que o campo 'id' é o primeiro
                        Long generatedId = generatedKeys.getLong(1);
                        
                        // Definir o id gerado na entidade
                        Field idField = entity.getClass().getDeclaredField("id");
                        idField.setAccessible(true);
                        idField.set(entity, generatedId);  // Atribui o id gerado
                    }
                }
            }
            
            System.out.println("Entidade salva com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public Optional<T> findById(ID id) {
        String tableName = getTableName();
        String idColumn = getIdColumnName(entityClass);

        String sql = "SELECT * FROM " + tableName + " WHERE " + idColumn + " = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                T entity = createInstanceFromResultSet(rs);
                return Optional.of(entity);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }
    
    @Override
    public List<T> findAll() {
        List<T> results = new ArrayList<>();
        String tableName = getTableName();

        String sql = "SELECT * FROM " + tableName;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                T entity = createInstanceFromResultSet(rs);
                results.add(entity);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }
    
    @Override
    public void update(T entity) {
        try {
            Map<String, Object> columnValues = getColumnNamesAndValues(entity);

            Object idValue = getIdValue(entity);
            if (idValue == null) {
                throw new IllegalStateException("ID não pode ser nulo para operação de update.");
            }

            StringBuilder sql = new StringBuilder("UPDATE ");
            sql.append(entity.getClass().getSimpleName().toLowerCase());
            sql.append(" SET ");

            columnValues.forEach((column, value) -> {
                sql.append(column).append(" = '").append(value).append("', ");
            });
            sql.deleteCharAt(sql.length() - 2); // Remove a última vírgula
            sql.append(" WHERE id = ").append(idValue);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {
                preparedStatement.executeUpdate();
            }

            System.out.println("Entidade atualizada com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void deleteById(ID id) {
        String tableName = getTableName();
        String sql = String.format("DELETE FROM %s WHERE id = ?", tableName);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar entidade com id: " + id, e);
        }
    }

    private String getTableName() {
        return entityClass.getSimpleName().toLowerCase();
    }
    
    public Map<String, Object> getColumnNamesAndValues(T entity) throws IllegalAccessException {
        Map<String, Object> columnValues = new HashMap<>();
        
        // Refletir sobre os campos da classe e de suas superclasses
        Class<?> clazz = entity.getClass();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                // Ignorar campos que são marcados como @Transient
                if (field.isAnnotationPresent(Transient.class)) {
                    continue;
                }
                
                // Ignorar o campo 'id', que deve ser gerado pelo banco (auto_increment)
                if (field.getName().equals("id")) {
                    continue;
                }

                field.setAccessible(true);  // Acessar o campo privado
                columnValues.put(field.getName(), field.get(entity));  // Adicionar no map
            }
            clazz = clazz.getSuperclass();  // Subir para a classe pai
        }
        
        return columnValues;
    }
    
    private String getIdColumnName(Class<?> clazz) {
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class) && field.isAnnotationPresent(Column.class)) {
                    return field.getAnnotation(Column.class).name();
                }
            }
            clazz = clazz.getSuperclass();
        }
        return "id"; // fallback
    }

    private Object getIdValue(T entity) {
        Class<?> clazz = entity.getClass();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true);
                    try {
                        return field.get(entity);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }
    
    private T createInstanceFromResultSet(ResultSet rs) {
        try {
            T instance = entityClass.getDeclaredConstructor().newInstance();
            Class<?> current = entityClass;

            while (current != null) {
                for (Field field : current.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Column.class)) {
                        Column coluna = field.getAnnotation(Column.class);
                        String columnName = coluna.name();

                        field.setAccessible(true);
                        Object value = rs.getObject(columnName);
                        field.set(instance, value);
                    }
                }
                current = current.getSuperclass(); // suporta herança
            }

            return instance;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao instanciar entidade", e);
        }
    }
}