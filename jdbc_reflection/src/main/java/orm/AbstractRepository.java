package orm;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.DatabaseConnection;
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
    
    @Override
    public void update(T entity) {
        Class<?> clazz = entityClass;
        String tableName = clazz.getSimpleName().toLowerCase();

        // Prepara a parte do SET para os campos
        StringBuilder setClause = new StringBuilder();
        List<Object> parameters = new ArrayList<>();
        Field idField = null;

        // Percorre os campos da classe e de suas superclasses
        for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(entity);
                    
                    // Ignora campos nulos
                    if (value != null) {
                        // Adiciona o campo no SET
                        if (setClause.length() > 0) setClause.append(", ");
                        setClause.append(field.getName()).append(" = ?");
                        parameters.add(value);
                    }

                    // Identifica o campo ID para a cláusula WHERE
                    if (field.getName().equalsIgnoreCase("id")) {
                        idField = field;
                    }

                } catch (IllegalAccessException e) {
                    System.err.println("Erro ao acessar o campo: " + field.getName() + " — " + e.getMessage());
                }
            }
        }

        if (idField == null) {
            throw new IllegalArgumentException("A classe não contém um campo 'id'!");
        }

        // Obtém o valor do campo ID
        Object idValue;
        try {
            idValue = idField.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Erro ao acessar o valor do ID", e);
        }

        if (idValue == null) {
            throw new IllegalArgumentException("O valor do campo 'id' não pode ser nulo!");
        }

        // Monta a query de update
        String sql = String.format("UPDATE %s SET %s WHERE id = ?", tableName, setClause);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Define os parâmetros do SET
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            // Define o parâmetro da cláusula WHERE
            stmt.setObject(parameters.size() + 1, idValue);

            // Executa a atualização
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar a entidade", e);
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
}