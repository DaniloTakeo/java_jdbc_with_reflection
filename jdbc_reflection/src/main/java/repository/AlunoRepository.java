package repository;

import java.sql.Connection;

import model.pessoa.Aluno;
import orm.AbstractRepository;

public class AlunoRepository extends AbstractRepository<Aluno, Long> {

	public AlunoRepository(Connection connection, Class<Aluno> entityClass) {
		super(connection, entityClass);
	}

	@Override
	public void deleteById(Long id) {
		
	}

}
