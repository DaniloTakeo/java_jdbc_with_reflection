package model.pagamento;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import model.curso.Curso;
import model.pessoa.Aluno;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Pagamento {
    
	private Long id;
	private BigDecimal valor;
    private Aluno aluno;
    private Curso curso;
    private PagamentoStatus status;
    private LocalDate dataVencimento;
    
    public boolean pagar(BigDecimal valor) {
        this.valor = valor;
        this.status = PagamentoStatus.PAGO;
        return true;
    }
    
    public PagamentoStatus consultarStatus() {
        return status;
    }
    
    public void cancelar() {
        this.status = PagamentoStatus.CANCELADO;
    }
    
    public void reembolsar() {
        this.status = PagamentoStatus.PENDENTE;
    }
}