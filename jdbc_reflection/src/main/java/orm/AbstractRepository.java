package orm;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractRepository<T, ID> implements GenericRepository<T, ID> {
    private final Class<T> entityClass;
    protected final Connection connection;

    public AbstractRepository(Connection connection, Class<T> entityClass) {
        this.connection = connection;
        this.entityClass = entityClass;
    }

    @Override
    public void save(T entity) {
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(getTableName()).append(" (");

        Field[] fields = entityClass.getDeclaredFields();
        List<Object> values = new ArrayList<>();

        for (Field field : fields) {
            if (field.getName().equalsIgnoreCase("id")) continue; // Ignora ID autogerado
            sql.append(field.getName()).append(", ");
        }
        sql.setLength(sql.length() - 2); // Remove última vírgula
        sql.append(") VALUES (");

        for (Field field : fields) {
            if (field.getName().equalsIgnoreCase("id")) continue;
            sql.append("?, ");
        }
        sql.setLength(sql.length() - 2); // Remove última vírgula
        sql.append(")");

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
            int index = 1;
            for (Field field : fields) {
                if (field.getName().equalsIgnoreCase("id")) continue;
                field.setAccessible(true);
                stmt.setObject(index++, field.get(entity));
            }

            stmt.executeUpdate();

            // Se quiser setar o ID gerado na entidade
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                Field idField = entityClass.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(entity, generatedKeys.getObject(1));
            }
        } catch (Exception e) {
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
        // Por padrão usa o nome da classe como nome da tabela
        return entityClass.getSimpleName().toLowerCase();
    }
}