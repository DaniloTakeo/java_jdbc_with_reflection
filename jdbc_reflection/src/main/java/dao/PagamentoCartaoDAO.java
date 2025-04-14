package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.curso.Curso;
import model.pagamento.PagamentoCartao;
import model.pagamento.PagamentoStatus;
import model.pessoa.Aluno;

public class PagamentoCartaoDAO {
	
	private static final String INSERT_PAGAMENTO_CARTAO = """
            INSERT INTO pagamento_cartao (valor, aluno_id, curso_id, status, data_vencimento, bandeira, numero_cartao, nome_titular, data_validade, cvv)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
	private static final String SELECT_PAGAMENTO_BY_ID = "SELECT * FROM pagamento_cartao WHERE id = ?";
	private static final String SELECT_ALL_PAGAMENTOS ="SELECT * FROM pagamento_cartao";
	private static final String UPDATE_PAGAMENTO = """
            UPDATE pagamento_cartao SET valor = ?, aluno_id = ?, curso_id = ?, status = ?, data_vencimento = ?, bandeira = ?, numero_cartao = ?, nome_titular = ?, data_validade = ?, cvv = ?
            WHERE id = ?
        """;
	private static final String DELETE_PAGAMENTO = "DELETE FROM pagamento_cartao WHERE id = ?";
    private final Connection connection;

    public PagamentoCartaoDAO(Connection connection) {
        this.connection = connection;
    }

    public void salvar(PagamentoCartao pagamento) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_PAGAMENTO_CARTAO, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setBigDecimal(1, pagamento.getValor());
            stmt.setLong(2, pagamento.getAluno().getId());
            stmt.setLong(3, pagamento.getCurso().getId());
            stmt.setString(4, pagamento.getStatus().name());
            stmt.setDate(5, Date.valueOf(pagamento.getDataVencimento()));
            stmt.setString(6, pagamento.getBandeira());
            stmt.setString(7, pagamento.getNumeroCartao());
            stmt.setString(8, pagamento.getNomeTitular());
            stmt.setString(9, pagamento.getDataValidade());
            stmt.setString(10, pagamento.getCvv());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    pagamento.setId(rs.getLong(1));
                }
            }
        }
    }

    public PagamentoCartao buscarPorId(Long id) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_PAGAMENTO_BY_ID)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapPagamento(rs);
                }
            }
        }
        return null;
    }

    public List<PagamentoCartao> buscarTodos() throws SQLException {
        List<PagamentoCartao> pagamentos = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_ALL_PAGAMENTOS);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                pagamentos.add(mapPagamento(rs));
            }
        }
        return pagamentos;
    }

    public void atualizar(PagamentoCartao pagamento) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_PAGAMENTO)) {
            stmt.setBigDecimal(1, pagamento.getValor());
            stmt.setLong(2, pagamento.getAluno().getId());
            stmt.setLong(3, pagamento.getCurso().getId());
            stmt.setString(4, pagamento.getStatus().name());
            stmt.setDate(5, Date.valueOf(pagamento.getDataVencimento()));
            stmt.setString(6, pagamento.getBandeira());
            stmt.setString(7, pagamento.getNumeroCartao());
            stmt.setString(8, pagamento.getNomeTitular());
            stmt.setString(9, pagamento.getDataValidade());
            stmt.setString(10, pagamento.getCvv());
            stmt.setLong(11, pagamento.getId());
            stmt.executeUpdate();
        }
    }

    public void deletar(Long id) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_PAGAMENTO)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    private PagamentoCartao mapPagamento(ResultSet rs) throws SQLException {
        PagamentoCartao pagamento = new PagamentoCartao();
        pagamento.setId(rs.getLong("id"));
        pagamento.setValor(rs.getBigDecimal("valor"));

        Aluno aluno = new Aluno();
        aluno.setId(rs.getLong("aluno_id"));
        pagamento.setAluno(aluno);

        Curso curso = new Curso();
        curso.setId(rs.getLong("curso_id"));
        pagamento.setCurso(curso);

        pagamento.setStatus(PagamentoStatus.valueOf(rs.getString("status")));
        pagamento.setDataVencimento(rs.getDate("data_vencimento").toLocalDate());
        pagamento.setBandeira(rs.getString("bandeira"));
        pagamento.setNumeroCartao(rs.getString("numero_cartao"));
        pagamento.setNomeTitular(rs.getString("nome_titular"));
        pagamento.setDataValidade(rs.getString("data_validade"));
        pagamento.setCvv(rs.getString("cvv"));

        return pagamento;
    }
}