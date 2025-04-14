package model.avaliacao;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.curso.Curso;
import model.pessoa.Aluno;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Avaliacao {
	
	private Long id;
    private Aluno aluno;
    private Curso curso;
    private double nota;
    private String comentario;

    public double calcularMedia(List<Avaliacao> avaliacoes) {
        if (avaliacoes == null || avaliacoes.isEmpty()) {
            return 0.0;
        }
        return avaliacoes.stream()
                .mapToDouble(Avaliacao::getNota)
                .average()
                .orElse(0.0);
    }
}