package model.pagamento;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class PagamentoPix extends Pagamento implements PagamentoService {
    private int tempoExpiracao;
    private String chavePix;
    
    @Override
    public boolean pagar(BigDecimal valor) {
        return super.pagar(valor);
    }
    
    @Override
    public PagamentoStatus consultarStatus(Pagamento pagamento) {
        return pagamento.consultarStatus();
    }
    
    public String gerarQrCode() {
        return "QR_CODE_123456";
    }
    
    public boolean validarExpiracao() {
        return tempoExpiracao > 0;
    }
}