package model.pessoa;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import model.curso.Curso;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class Instrutor extends Pessoa {
	private Long id;
    private String biografia;
    private List<Curso> cursosCriados;

    public Curso criarCurso(String titulo, String descricao, BigDecimal preco) {
        Curso curso = new Curso(null, titulo, descricao, preco, this, new ArrayList<>(), new ArrayList<>());
        if (cursosCriados == null) {
            cursosCriados = new ArrayList<>();
        }
        cursosCriados.add(curso);
        return curso;
    }
     public void adicionarCursoCriado(Curso curso) {
        if (cursosCriados == null) {
            cursosCriados = new ArrayList<>();
        }
        cursosCriados.add(curso);
    }
}