package model.pagamento;

import java.math.BigDecimal;

public interface PagamentoService {
    
	boolean pagar(BigDecimal valor);
    PagamentoStatus consultarStatus(Pagamento pagamento);
}