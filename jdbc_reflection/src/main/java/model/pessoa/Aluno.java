package model.pessoa;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import model.curso.Curso;
import model.pagamento.Pagamento;
import orm.annotations.Column;
import orm.annotations.Table;
import orm.annotations.Transient;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Table(name = "aluno")
public class Aluno extends Pessoa {
	
	@Column(name = "id")
	private Long id;
	
	@Column(name = "matricula")
	private String matricula;
	
	@Transient
    private List<Curso> listaDeCursos;
	
	@Transient
    private List<Pagamento> pagamentos;

    public void inscreverNoCurso(Curso curso) {
        if (listaDeCursos == null) {
            listaDeCursos = new ArrayList<>();
        }
        listaDeCursos.add(curso);
    }

    public List<Pagamento> obterPagamentos() {
        return pagamentos;
    }

    public List<Curso> listarCursos() {
        return listaDeCursos;
    }
  
}