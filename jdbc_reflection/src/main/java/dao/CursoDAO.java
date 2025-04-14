package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.curso.Curso;

public class CursoDAO {
	private final static String INSERT_CURSO = "INSERT INTO Curso (titulo, descricao, preco, instrutor_id) VALUES (?, ?, ?, ?)";
	private final static String SELECT_CURSO_BY_ID = "SELECT * FROM Curso WHERE id = ?";
	private final static String SELECT_ALL_CURSOS = "SELECT * FROM Curso";
	private final static String UPDATE_CURSO = "UPDATE Curso SET titulo = ?, descricao = ?, preco = ?, instrutor_id = ? WHERE id = ?";
	private final static String DELETE_CURSO = "DELETE FROM Curso WHERE id = ?";
	private final static String INSERT_ALUNO_INTO_CURSO = "INSERT INTO aluno_curso(aluno_id, curso_id) VALUES (?, ?)";
	private final Connection connection;
    private InstrutorDAO instrutorDao;
    private AlunoDAO alunoDao;

    public CursoDAO(Connection connection, AlunoDAO alunoDAO, InstrutorDAO instrutorDAO) {
        this.connection = connection;
        this.instrutorDao = instrutorDAO;
        this.alunoDao = alunoDAO;
    }

    public void salvar(Curso curso) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_CURSO, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, curso.getTitulo());
            stmt.setString(2, curso.getDescricao());
            stmt.setBigDecimal(3, curso.getPreco());
            stmt.setLong(4, curso.getInstrutor().getId());
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    curso.setId(generatedKeys.getLong(1));
                }
            }
        }
    }

    public Curso buscarPorId(Long id) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_CURSO_BY_ID)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapCurso(rs, id);
                }
            }
        }
        return null;
    }

    public List<Curso> buscarTodos() throws SQLException {
        List<Curso> cursos = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_ALL_CURSOS);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                cursos.add(mapCurso(rs, rs.getLong("id")));
            }
        }
        return cursos;
    }

    public void atualizar(Curso curso) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_CURSO)) {
            stmt.setString(1, curso.getTitulo());
            stmt.setString(2, curso.getDescricao());
            stmt.setBigDecimal(3, curso.getPreco());
            stmt.setLong(4, curso.getInstrutor().getId());
            stmt.setLong(5, curso.getId());
            stmt.executeUpdate();
        }
    }

    public void deletar(Long id) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_CURSO)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
    
    public void salvarAlunoNoCurso(Long alunoId, Long cursoId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_ALUNO_INTO_CURSO, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, alunoId);
            stmt.setLong(2, cursoId);
            stmt.executeUpdate();
        }
    }
    
    private Curso mapCurso(ResultSet rs, Long id) throws SQLException {
    	Curso curso = new Curso(
                rs.getLong("id"),
                rs.getString("titulo"),
                rs.getString("descricao"),
                rs.getBigDecimal("preco"),
                instrutorDao.buscarPorId(rs.getLong("instrutor_id")),
                null,
                alunoDao.buscarPorCurso(id));
    	
    	return curso;
    }
}