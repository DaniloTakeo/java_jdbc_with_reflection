package repository;

import java.sql.Connection;
import java.util.List;

import model.pessoa.Aluno;
import orm.AbstractRepository;

public class AlunoRepository extends AbstractRepository<Aluno, Long> {

	public AlunoRepository(Connection connection, Class<Aluno> entityClass) {
		super(connection, entityClass);
	}

	@Override
	public List<Aluno> findAll() {
		return null;
	}

	@Override
	public void update(Aluno entity) {
		
	}

	@Override
	public void deleteById(Long id) {
		
	}

}
