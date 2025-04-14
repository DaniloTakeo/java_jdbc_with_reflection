package model.curso;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Aula {
	
	private Long id;
    private String titulo;
    private String conteudo;
    private int duracao;
    private Modulo modulo;
}
