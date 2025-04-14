package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.pessoa.Instrutor;

public class InstrutorDAO {
	private static final String INSERT_INSTRUTOR = "INSERT INTO instrutor (nome, email, senha, biografia) VALUES (?, ?, ?, ?)";
    private static final String SELECT_INSTRUTOR_BY_ID = "SELECT * FROM instrutor WHERE id = ?";
    private static final String SELECT_ALL_INSTRUTORES = "SELECT * FROM instrutor";
    private static final String SELECT_INSTRUTOR_BY_EMAIL = "SELECT * FROM instrutor WHERE email = ?";
    private static final String UPDATE_INSTRUTOR = "UPDATE instrutor SET nome = ?, email = ?, senha = ?, biografia = ? WHERE id = ?";;
	private Connection connection;

    public InstrutorDAO(Connection connection) {
        this.connection = connection;
    }

    public void inserir(Instrutor instrutor) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_INSTRUTOR, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, instrutor.getNome());
            stmt.setString(2, instrutor.getEmail());
            stmt.setString(3, instrutor.getSenha());
            stmt.setString(4, instrutor.getBiografia());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    instrutor.setId(generatedKeys.getLong(1));
                }
            }
        }
    }

    public Instrutor buscarPorId(Long id) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_INSTRUTOR_BY_ID)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                	return mapInstrutor(rs);
                }
            }
        }
        return null;
    }
    
    public Instrutor buscarPorEmail(String email) {
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_INSTRUTOR_BY_EMAIL)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
            	return mapInstrutor(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar aluno por e-mail", e);
        }
        
        return null;
    }

    public List<Instrutor> buscarTodos() throws SQLException {
        List<Instrutor> instrutores = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_ALL_INSTRUTORES);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                instrutores.add(mapInstrutor(rs));
            }
        }
        return instrutores;
    }

    public void atualizar(Instrutor instrutor) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_INSTRUTOR)) {
            stmt.setString(1, instrutor.getNome());
            stmt.setString(2, instrutor.getEmail());
            stmt.setString(3, instrutor.getSenha());
            stmt.setString(4, instrutor.getBiografia());
            stmt.setLong(5, instrutor.getId());
            stmt.executeUpdate();
        }
    }

    public void deletar(Long id) throws SQLException {
        String sql = "DELETE FROM instrutor WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
    
    private Instrutor mapInstrutor(ResultSet rs) throws SQLException {
    	return Instrutor.builder().id(rs.getLong("id"))
    			.nome(rs.getString("nome"))
    			.email(rs.getString("email"))
    			.senha("senha")
    			.biografia("biografia")
    			.build();
    }
}
