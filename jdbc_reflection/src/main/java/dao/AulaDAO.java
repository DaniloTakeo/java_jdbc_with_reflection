package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.curso.Aula;

public class AulaDAO {

	private static final String INSERT_AULA = "INSERT INTO aula (conteudo, duracao) VALUES (?, ?)";
	private static final String SELECT_ALL_AULAS = "SELECT * FROM aula";
	private static final String SELECT_AULA_BY_ID = "SELECT * FROM aula WHERE id = ?";
	private static final String SELECT_AULA_BY_MODULO_ID = "SELECT * FROM aula WHERE modulo_id = ?";
	private static final String DELETE_AULA_BY_MODULO_ID = "DELETE FROM aula WHERE modulo_id = ?";
	private static final String UPDATE_AULA = "UPDATE aula SET conteudo = ?, duracao = ? WHERE id = ?";
	private static final String DELETE_AULA_BY_ID = "DELETE FROM aula WHERE id = ?";
    private final Connection connection;
    private ModuloDAO moduloDao;
    
    public AulaDAO(Connection connection) {
        this.connection = connection;
    }

    public AulaDAO(Connection connection, ModuloDAO moduloDao) {
        this.connection = connection;
        this.moduloDao = moduloDao;
    }

    public void salvar(Aula aula) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_AULA, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, aula.getConteudo());
            stmt.setInt(2, aula.getDuracao());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    aula.setId(rs.getLong(1));
                }
            }
        }
    }
    
    public List<Aula> buscarTodas() throws SQLException {
        List<Aula> aulas = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_ALL_AULAS);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                aulas.add(mapAula(rs));
            }
        }
        return aulas;
    }

    public Aula buscarPorId(Long id) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_AULA_BY_ID)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapAula(rs);
                }
            }
        }
        return null;
    }
    
    public List<Aula> buscarPorModuloId(Long moduloId) throws SQLException {
        List<Aula> aulas = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_AULA_BY_MODULO_ID)) {
            stmt.setLong(1, moduloId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    aulas.add(mapAula(rs));
                }
            }
        }
        return aulas;
    }

    public void deletarPorModuloId(Long moduloId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_AULA_BY_MODULO_ID)) {
            stmt.setLong(1, moduloId);
            stmt.executeUpdate();
        }
    }

    public void atualizar(Aula aula) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_AULA)) {
            stmt.setString(1, aula.getConteudo());
            stmt.setInt(2, aula.getDuracao());
            stmt.setLong(3, aula.getId());
            stmt.executeUpdate();
        }
    }

    public void deletarPorId(Long id) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_AULA_BY_ID)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

	public void setModuloDao(ModuloDAO moduloDao) {
		this.moduloDao = moduloDao;
	}
	
	private Aula mapAula(ResultSet rs) throws SQLException {
		Aula aula = new Aula();
        aula.setId(rs.getLong("id"));
        aula.setConteudo(rs.getString("conteudo"));
        aula.setDuracao(rs.getInt("duracao"));
        aula.setModulo(moduloDao.buscarPorId(rs.getLong("modulo_id")));
        
        return aula;
	}
}