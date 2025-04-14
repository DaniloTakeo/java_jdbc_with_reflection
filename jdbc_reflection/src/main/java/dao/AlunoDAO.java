package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.pessoa.Aluno;

public class AlunoDAO {
    private static final String INSERT_ALUNO = "INSERT INTO aluno (nome, email, senha, matricula) VALUES (?, ?, ?, ?)";
    private static final String SELECT_ALUNO_BY_ID = "SELECT * FROM aluno WHERE id = ?";
    private static final String SELECT_ALUNO_BY_EMAIL = "SELECT id, nome, email, senha, matricula FROM aluno WHERE email = ?";
    private static final String SELECT_ALL_ALUNOS = "SELECT * FROM aluno";
    private static final String UPDATE_ALUNO = "UPDATE aluno SET nome = ?, email = ?, senha = ?, matricula = ? WHERE id = ?";
    private static final String DELETE_ALUNO = "DELETE FROM aluno WHERE id = ?";
    private static final String SELECT_ALUNOS_BY_CURSO = "SELECT * FROM aluno_curso WHERE curso_id = ?";
    private Connection connection;
    
    public AlunoDAO(Connection connection) {
    	this.connection = connection;
    }
    
    public void salvar(Aluno aluno) {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_ALUNO, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, aluno.getNome());
            stmt.setString(2, aluno.getEmail());
            stmt.setString(3, aluno.getSenha());
            stmt.setString(4, aluno.getMatricula());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    aluno.setId(Long.valueOf(generatedKeys.getInt(1)));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Aluno buscarPorId(Long id) {
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_ALUNO_BY_ID)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
            	return mapAluno(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public Aluno buscarPorEmail(String email) {
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_ALUNO_BY_EMAIL)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
            	return mapAluno(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar aluno por e-mail", e);
        }
        
        return null;
    }

    public List<Aluno> buscarTodos() {
        List<Aluno> alunos = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_ALL_ALUNOS);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                alunos.add(mapAluno(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alunos;
    }

    public void atualizar(Aluno aluno) {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_ALUNO)) {

            stmt.setString(1, aluno.getNome());
            stmt.setString(2, aluno.getEmail());
            stmt.setString(3, aluno.getSenha());
            stmt.setString(4, aluno.getMatricula());
            stmt.setLong(5, aluno.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletar(Long id) {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_ALUNO)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public List<Aluno> buscarPorCurso(Long idCurso) {
    	List<Aluno> alunos = new ArrayList<Aluno>();
    	try(PreparedStatement stmt = connection.prepareStatement(SELECT_ALUNOS_BY_CURSO)) {
			stmt.setLong(1, idCurso);
			ResultSet rs = stmt.executeQuery();
    		
    		while(rs.next()) {
    			this.buscarPorId(rs.getLong("id_aluno"));
    			alunos.add(mapAluno(rs));
    		}
    		
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    	
		return alunos;
    }
    
    private Aluno mapAluno(ResultSet rs) throws SQLException {
    	return Aluno.builder().id(rs.getLong("id"))
		.nome(rs.getString("nome"))
		.email(rs.getString("email"))
		.senha(rs.getString("senha"))
		.matricula(rs.getString("matricula"))
		.build();
    }
}
