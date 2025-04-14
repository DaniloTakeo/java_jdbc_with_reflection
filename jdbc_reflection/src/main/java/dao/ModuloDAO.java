package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.curso.Aula;
import model.curso.Modulo;

public class ModuloDAO {

	private static final String INSERT_MODULO = "INSERT INTO modulo (titulo) VALUES (?)";
	private static final String SELECT_MODULO_BY_ID = "SELECT * FROM modulo WHERE id = ?";
	private static final String SELECT_ALL_MODULOS = "SELECT * FROM modulo";
	private static final String UPDATE_MODULO = "UPDATE modulo SET titulo = ? WHERE id = ?";
	private static final String DELETE_MODULO_BY_ID = "DELETE FROM modulo WHERE id = ?";
    private final Connection connection;
    private AulaDAO aulaDao;

    public ModuloDAO(Connection connection, AulaDAO aulaDao) {
        this.connection = connection;
        this.aulaDao = aulaDao;
    }

    public void salvar(Modulo modulo) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_MODULO, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, modulo.getTitulo());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    modulo.setId(generatedKeys.getLong(1));
                }
            }
        }
    }

    public Modulo buscarPorId(Long id) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_MODULO_BY_ID)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapModulo(rs);
                }
            }
        }
        return null;
    }

    public List<Modulo> listarTodos() throws SQLException {
        List<Modulo> modulos = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_ALL_MODULOS);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                modulos.add(mapModulo(rs));
            }
        }
        return modulos;
    }

    public void atualizar(Modulo modulo) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_MODULO)) {
            stmt.setString(1, modulo.getTitulo());
            stmt.setLong(2, modulo.getId());
            stmt.executeUpdate();
        }
    }

    public void deletar(Long id) throws SQLException {
        AulaDAO aulaDAO = new AulaDAO(connection);
        aulaDAO.deletarPorModuloId(id);

        try (PreparedStatement stmt = connection.prepareStatement(DELETE_MODULO_BY_ID)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
    
    private Modulo mapModulo(ResultSet rs) throws SQLException {
    	Modulo modulo = new Modulo();
        modulo.setId(rs.getLong("id"));
        modulo.setTitulo(rs.getString("titulo"));

        List<Aula> aulas = aulaDao.buscarPorModuloId(modulo.getId());
        modulo.setAulas(aulas);
        
        return modulo;
    }
}
