package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.avaliacao.Avaliacao;
import model.curso.Curso;
import model.pessoa.Aluno;

public class AvaliacaoDAO {
	
	private static final String INSERT_AVALIACAO = "INSERT INTO avaliacao (aluno_id, curso_id, nota, comentario) VALUES (?, ?, ?, ?)";
	private static final String SELECT_ALL_AVALIACOES = "SELECT * FROM avaliacao";
	private static final String SELECT_AVALIACAO_BY_ID = "SELECT * FROM avaliacao WHERE id = ?";
	private static final String SELECT_AVALIACAO_BY_CURSO_ID = "SELECT * FROM avaliacao WHERE curso_id = ?";
	private static final String SELECT_AVALIACAO_BY_ALUNO_ID = "SELECT * FROM avaliacao WHERE aluno_id = ?";
	private static final String UPDATE_AVALIACAO = "UPDATE avaliacao SET aluno_id = ?, curso_id = ?, nota = ?, comentario = ? WHERE id = ?";
	private static final String DELETE_AVALIACAO = "DELETE FROM avaliacao WHERE id = ?";
    private final Connection connection;

    public AvaliacaoDAO(Connection connection) {
        this.connection = connection;
    }

    public void salvar(Avaliacao avaliacao) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_AVALIACAO, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, avaliacao.getAluno().getId());
            stmt.setLong(2, avaliacao.getCurso().getId());
            stmt.setDouble(3, avaliacao.getNota());
            stmt.setString(4, avaliacao.getComentario());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    avaliacao.setId(rs.getLong(1));
                }
            }
        }
    }

    public List<Avaliacao> buscarTodas() throws SQLException {
        List<Avaliacao> avaliacoes = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_ALL_AVALIACOES);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                avaliacoes.add(mapAvaliacao(rs));
            }
        }
        return avaliacoes;
    }

    public Avaliacao buscarPorId(Long id) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_AVALIACAO_BY_ID)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapAvaliacao(rs);
                }
            }
        }
        return null;
    }
    
    public List<Avaliacao> buscarPorCurso(Long cursoId) throws SQLException {
        List<Avaliacao> avaliacoes = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_AVALIACAO_BY_CURSO_ID)) {
            stmt.setLong(1, cursoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    avaliacoes.add(mapAvaliacao(rs));
                }
            }
        }
        return avaliacoes;
    }
    
    public List<Avaliacao> buscarPorAluno(Long alunoId) throws SQLException {
        List<Avaliacao> avaliacoes = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_AVALIACAO_BY_ALUNO_ID)) {
            stmt.setLong(1, alunoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    avaliacoes.add(mapAvaliacao(rs));
                }
            }
        }
        return avaliacoes;
    }
    
    public void atualizar(Avaliacao avaliacao) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_AVALIACAO)) {
            stmt.setLong(1, avaliacao.getAluno().getId());
            stmt.setLong(2, avaliacao.getCurso().getId());
            stmt.setDouble(3, avaliacao.getNota());
            stmt.setString(4, avaliacao.getComentario());
            stmt.setLong(5, avaliacao.getId());
            stmt.executeUpdate();
        }
    }
    
    public void deletar(Long id) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_AVALIACAO)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
    
    private Avaliacao mapAvaliacao(ResultSet rs) throws SQLException {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setId(rs.getLong("id"));
        avaliacao.setNota(rs.getDouble("nota"));
        avaliacao.setComentario(rs.getString("comentario"));

        Aluno aluno = new Aluno();
        aluno.setId(rs.getLong("aluno_id"));

        Curso curso = new Curso();
        curso.setId(rs.getLong("curso_id"));

        avaliacao.setAluno(aluno);
        avaliacao.setCurso(curso);
        return avaliacao;
    }
}
