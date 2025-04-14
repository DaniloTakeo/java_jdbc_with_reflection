package model.curso;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Modulo {
	private Long id;
    private String titulo;
    private List<Aula> aulas;
    
    public Modulo(String titulo) {
    	this.titulo = titulo;
    }
    
    public Modulo(String titulo, List<Aula> aulas) {
    	this.titulo = titulo;
    	this.aulas = aulas;
    }

     public void adicionarAula(Aula aula) {
        if(aulas == null){
            aulas = new ArrayList<>();
        }
        aulas.add(aula);
    }
}