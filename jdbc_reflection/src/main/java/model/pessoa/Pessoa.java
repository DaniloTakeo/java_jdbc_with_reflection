package model.pessoa;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import orm.annotations.Column;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Pessoa {
	
	@Column(name = "nome")
    protected String nome;
	
	@Column(name = "email")
    protected String email;
	
	@Column(name = "senha")
    protected String senha;
    
}