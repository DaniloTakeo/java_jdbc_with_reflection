package main;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import config.DatabaseConnection;
import dao.AlunoDAO;
import dao.AulaDAO;
import dao.CursoDAO;
import dao.InstrutorDAO;
import dao.ModuloDAO;
import model.curso.Curso;
import model.curso.Modulo;
import model.pagamento.Pagamento;
import model.pagamento.PagamentoCartao;
import model.pagamento.PagamentoPix;
import model.pagamento.PagamentoStatus;
import model.pessoa.Aluno;
import model.pessoa.Instrutor;
import repository.AlunoRepository;

public class TestePlataforma {
	private static AlunoDAO alunoDao;
	private static AlunoRepository alunoRepository;
	private static InstrutorDAO instrutorDao;
	private static CursoDAO cursoDao;
	private static AulaDAO aulaDao;
	private static ModuloDAO moduloDao;
	private static List<Aluno> alunos = new ArrayList<>();
	private static List<Instrutor> instrutores = new ArrayList<>();
	private static List<Curso> cursos = new ArrayList<>();
	private static List<Pagamento> pagamentos = new ArrayList<>();
	private static Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) throws InterruptedException, SQLException {
		Thread.sleep(10000);
		Connection conn = DatabaseConnection.getConnection();
		alunoDao = new AlunoDAO(conn);
		instrutorDao = new InstrutorDAO(conn);
		cursoDao = new CursoDAO(conn, alunoDao, instrutorDao);
		aulaDao = new AulaDAO(conn);
		moduloDao = new ModuloDAO(conn, aulaDao);
		aulaDao.setModuloDao(moduloDao);
		
		alunoRepository = new AlunoRepository(conn, Aluno.class);
		
		try (Connection connection = DatabaseConnection.getConnection()) {
			System.out.println("Conexão estabelecida com sucesso!");
		} catch (SQLException e) {
			System.err.println("Falha ao conectar ao banco: " + e.getMessage());
		}

		menuPrincipal();
	}

	private static void menuPrincipal() throws SQLException {
		while (true) {
			System.out.println("\n--- Menu Principal ---");
			System.out.println("1 - Aluno");
			System.out.println("2 - Instrutor");
			System.out.println("3 - Curso");
			System.out.println("4 - Pagamento");
			System.out.println("0 - Sair");
			System.out.print("Escolha uma opção: ");
			int opcao = scanner.nextInt();
			scanner.nextLine();

			switch (opcao) {
			case 1:
				menuAluno();
				break;
			case 2:
				menuInstrutor();
				break;
			case 3:
				menuCurso();
				break;
			case 4:
				menuPagamento();
				break;
			case 5:
				menuPrincipal();
				break;
			case 0:
				System.out.println("Saindo do sistema...");
				scanner.close();
				return;
			default:
				System.out.println("Opção inválida. Tente novamente.");
			}
		}
	}

	private static void menuAluno() {
		while (true) {
			System.out.println("\n--- Menu Aluno ---");
			System.out.println("1 - Criar Aluno");
			System.out.println("2 - Listar Alunos");
			System.out.println("3 - Atualizar Aluno");
			System.out.println("4 - Deletar Aluno");
			System.out.println("0 - Voltar");
			System.out.print("Escolha uma opção: ");
			int opcao = scanner.nextInt();
			scanner.nextLine();

			switch (opcao) {
			case 1:
				criarAluno();
				break;
			case 2:
				listarAlunos();
				break;
			case 3:
				atualizarAluno();
				break;
			case 4:
				deletarAluno();
				break;
			case 0:
				return;
			default:
				System.out.println("Opção inválida. Tente novamente.");
			}
		}
	}

	private static void criarAluno() {
		System.out.print("Nome do aluno: ");
		String nome = scanner.nextLine();
		System.out.print("Email do aluno: ");
		String email = scanner.nextLine();
		System.out.print("Senha do aluno: ");
		String senha = scanner.nextLine();
		System.out.print("Matrícula do aluno: ");
		String matricula = scanner.nextLine();
		Aluno aluno = Aluno.builder().matricula(matricula).nome(nome).email(email).senha(senha).build();
		alunoRepository.save(aluno);
		System.out.println("Aluno criado com sucesso!");
	}

	private static void listarAlunos() {
		List<Aluno> alunos = alunoRepository.findAll();
		if (alunos.isEmpty()) {
			System.out.println("Não há alunos cadastrados.");
			return;
		}
		System.out.println("\n--- Lista de Alunos ---");
		for (int i = 0; i < alunos.size(); i++) {
			Aluno aluno = alunos.get(i);
			System.out.println((i + 1) + " ID: " + aluno.getId() + ". Nome: " + aluno.getNome() + ", Email: "
					+ aluno.getEmail() + ", Matrícula: " + aluno.getMatricula());
		}
	}

	private static void atualizarAluno() {
		List<Aluno> alunos = alunoRepository.findAll();
		if (alunos.isEmpty()) {
			System.out.println("Não há alunos cadastrados.");
			return;
		}

		System.out.print("Digite o id do aluno que deseja atualizar: ");
		int id = scanner.nextInt();
		scanner.nextLine();
		Aluno aluno = alunoRepository.findById(Long.valueOf(id)).get();
		System.out.print("Novo nome do aluno (ou deixe em branco para manter o mesmo): ");
		String novoNome = scanner.nextLine();
		if (!novoNome.isEmpty()) {
			aluno.setNome(novoNome);
		}
		System.out.print("Novo email do aluno (ou deixe em branco para manter o mesmo): ");
		String novoEmail = scanner.nextLine();
		if (!novoEmail.isEmpty()) {
			aluno.setEmail(novoEmail);
		}
		System.out.print("Nova senha do aluno (ou deixe em branco para manter o mesmo): ");
		String novaSenha = scanner.nextLine();
		if (!novaSenha.isEmpty()) {
			aluno.setSenha(novaSenha);
		}
		System.out.print("Nova matrícula do aluno (ou deixe em branco para manter o mesmo): ");
		String novaMatricula = scanner.nextLine();
		if (!novaMatricula.isEmpty()) {
			aluno.setMatricula(novaMatricula);
		}

		alunoRepository.update(aluno);

		System.out.println("Aluno atualizado com sucesso!");
	}

	private static void deletarAluno() {
		List<Aluno> alunos = alunoDao.buscarTodos();
		if (alunos.isEmpty()) {
			return;
		}
		System.out.print("Digite o id do aluno que deseja deletar: ");
		int id = scanner.nextInt();
		scanner.nextLine();

		alunoRepository.deleteById(Long.valueOf(id));
		System.out.println("Aluno deletado com sucesso!");
	}

	private static void menuInstrutor() throws SQLException {
		while (true) {
			System.out.println("\n--- Menu Instrutor ---");
			System.out.println("1 - Criar Instrutor");
			System.out.println("2 - Listar Instrutores");
			System.out.println("3 - Atualizar Instrutor");
			System.out.println("4 - Deletar Instrutor");
			System.out.println("0 - Voltar");
			System.out.print("Escolha uma opção: ");
			int opcao = scanner.nextInt();
			scanner.nextLine();

			switch (opcao) {
			case 1:
				criarInstrutor();
				break;
			case 2:
				listarInstrutores();
				break;
			case 3:
				atualizarInstrutor();
				break;
			case 4:
				deletarInstrutor();
				break;
			case 0:
				return;
			default:
				System.out.println("Opção inválida. Tente novamente.");
			}
		}
	}

	private static void criarInstrutor() throws SQLException {
		System.out.print("Nome do instrutor: ");
		String nome = scanner.nextLine();
		
		System.out.print("Email do instrutor: ");
		String email = scanner.nextLine();
		
		System.out.print("Senha do instrutor: ");
		String senha = scanner.nextLine();
		
		System.out.print("Biografia do instrutor: ");
		String biografia = scanner.nextLine();
		
		Instrutor instrutor = Instrutor.builder().nome(nome).email(email)
				.senha(senha).biografia(biografia).build();
		instrutorDao.inserir(instrutor);
		
		System.out.println("Instrutor criado com sucesso!");
	}

	private static void listarInstrutores() throws SQLException {
		instrutores = instrutorDao.buscarTodos();
		if (instrutores.isEmpty()) {
			System.out.println("Não há instrutores cadastrados.");
			return;
		}
		System.out.println("\n--- Lista de Instrutores ---");
		for (int i = 0; i < instrutores.size(); i++) {
			Instrutor instrutor = instrutores.get(i);
			System.out.println((i + 1) + ". Nome: " + instrutor.getNome() + ", Email: " + instrutor.getEmail()
					+ ", Biografia: " + instrutor.getBiografia());
		}
	}

	private static void atualizarInstrutor() throws SQLException {
		listarInstrutores();
		if (instrutores.isEmpty()) {
			return;
		}
		System.out.print("Digite o id do instrutor que deseja atualizar: ");
		int id = scanner.nextInt() - 1;
		scanner.nextLine();

		if (id >= 0 && id < instrutores.size()) {
			Instrutor instrutor = instrutores.get(id);
			System.out.print("Novo nome do instrutor (ou deixe em branco para manter o mesmo): ");
			String novoNome = scanner.nextLine();
			if (!novoNome.isEmpty()) {
				instrutor.setNome(novoNome);
			}
			System.out.print("Novo email do instrutor (ou deixe em branco para manter o mesmo): ");
			String novoEmail = scanner.nextLine();
			if (!novoEmail.isEmpty()) {
				instrutor.setEmail(novoEmail);
			}
			System.out.print("Nova senha do instrutor (ou deixe em branco para manter o mesmo): ");
			String novaSenha = scanner.nextLine();
			if (!novaSenha.isEmpty()) {
				instrutor.setSenha(novaSenha);
			}
			System.out.print("Nova biografia do instrutor (ou deixe em branco para manter o mesmo): ");
			String novaBiografia = scanner.nextLine();
			if (!novaBiografia.isEmpty()) {
				instrutor.setBiografia(novaBiografia);
			}
			instrutorDao.atualizar(instrutor);
			System.out.println("Instrutor atualizado com sucesso!");
		} else {
			System.out.println("Id inválido.");
		}
	}

	private static void deletarInstrutor() throws SQLException {
		listarInstrutores();
		if (instrutores.isEmpty()) {
			return;
		}
		System.out.print("Digite o id do instrutor que deseja deletar: ");
		Long id = scanner.nextLong();
		scanner.nextLine();

		instrutorDao.deletar(id);
		System.out.println("Instrutor deletado com sucesso!");
	}

	private static void menuCurso() throws SQLException {
		while (true) {
			System.out.println("\n--- Menu Curso ---");
			System.out.println("1 - Criar Curso");
			System.out.println("2 - Listar Cursos");
			System.out.println("3 - Atualizar Curso");
			System.out.println("4 - Deletar Curso");
			System.out.println("5 - Adicionar Aluno ao Curso");
			System.out.println("6 - Menu Módulo");
			System.out.println("7 - Menu Aula");
			System.out.println("0 - Voltar");
			System.out.print("Escolha uma opção: ");
			int opcao = scanner.nextInt();
			scanner.nextLine();

			switch (opcao) {
			case 1:
				criarCurso();
				break;
			case 2:
				listarCursos();
				break;
			case 3:
				atualizarCurso();
				break;
			case 4:
				deletarCurso();
				break;
			case 5:
				adicionarAlunoAoCurso();
				break;
			case 6:
				menuModulo();
				break;
			case 7:
				menuAula();
				break;
			case 0:
				return;
			default:
				System.out.println("Opção inválida. Tente novamente.");
			}
		}
	}

	private static void criarCurso() throws SQLException {
		System.out.print("Título do curso: ");
		String titulo = scanner.nextLine();
		System.out.print("Descrição do curso: ");
		String descricao = scanner.nextLine();
		System.out.print("Preço do curso: ");
		double precoDouble = scanner.nextDouble();
		BigDecimal preco = BigDecimal.valueOf(precoDouble);
		System.out.println("Instrutor responsável: ");
		String instrutorNome = scanner.nextLine();
		Optional<Instrutor> instrutor = instrutores.stream().filter(i -> i.getNome().equals(instrutorNome)).findFirst();

		scanner.nextLine();

		Curso curso = new Curso(titulo, descricao, preco, instrutor.get(), new ArrayList<>(), new ArrayList<>());
		cursoDao.salvar(curso);
		System.out.println("Curso criado com sucesso!");
	}

	private static void listarCursos() throws SQLException {
		List<Curso> cursos = cursoDao.buscarTodos();
		if (cursos.isEmpty()) {
			System.out.println("Não há cursos cadastrados.");
			return;
		}
		System.out.println("\n--- Lista de Cursos ---");
		for (int i = 0; i < cursos.size(); i++) {
			Curso curso = cursos.get(i);
			System.out.println((i + 1) + ". Título: " + curso.getTitulo() + ", Descrição: " + curso.getDescricao()
					+ ", Preço: " + curso.getPreco());
		}
	}

	private static void atualizarCurso() throws SQLException {
		listarCursos();
		if (cursos.isEmpty()) {
			return;
		}
		System.out.print("Digite o id do curso que deseja atualizar: ");
		Long cursoId = scanner.nextLong();
		scanner.nextLine();

		Curso curso = cursoDao.buscarPorId(cursoId);
		System.out.print("Novo título do curso (ou deixe em branco para manter o mesmo): ");
		String novoTitulo = scanner.nextLine();
		if (!novoTitulo.isEmpty()) {
			curso.setTitulo(novoTitulo);
		}
		System.out.print("Nova descrição do curso (ou deixe em branco para manter o mesmo): ");
		String novaDescricao = scanner.nextLine();
		if (!novaDescricao.isEmpty()) {
			curso.setDescricao(novaDescricao);
		}
		System.out.print("Novo preço do curso (ou deixe em branco para manter o mesmo): ");
		if (scanner.hasNextDouble()) {
			double novoPrecoDouble = scanner.nextDouble();
			BigDecimal novoPreco = BigDecimal.valueOf(novoPrecoDouble);
			scanner.nextLine();
			curso.setPreco(novoPreco);
		}
		
		cursoDao.atualizar(curso);
		System.out.println("Curso atualizado com sucesso!");
	}

	private static void deletarCurso() throws SQLException {
		listarCursos();
		if (cursos.isEmpty()) {
			return;
		}
		System.out.print("Digite o id do curso que deseja deletar: ");
		Long cursoId = scanner.nextLong();
		scanner.nextLine();

		cursoDao.deletar(cursoId);
		System.out.println("Curso deletado com sucesso!");
	}

	private static void adicionarAlunoAoCurso() throws SQLException {
		List<Curso> cursos = cursoDao.buscarTodos();
		List<Aluno> alunos = alunoDao.buscarTodos();
		if (cursos.isEmpty()) {
			System.out.println("Não há cursos cadastrados.");
			return;
		}
		
		if (alunos.isEmpty()) {
			System.out.println("Não há alunos cadastrados.");
			return;
		}

		listarCursos();
		System.out.print("Digite o número do curso: ");
		Long cursoId = scanner.nextLong();
		scanner.nextLine();

		listarAlunos();
		System.out.print("Digite o número do aluno: ");
		Long alunoId = scanner.nextLong();
		scanner.nextLine();
		
		cursoDao.salvarAlunoNoCurso(alunoId, cursoId);
		System.out.println("Aluno adicionado ao curso com sucesso!");

	}

	private static void adicionarModuloAoCurso() throws SQLException {
		listarCursos();
		if (cursos.isEmpty()) {
			System.out.println("Não há cursos cadastrados.");
			return;
		}

		System.out.print("Digite o id do curso ao qual deseja adicionar um módulo: ");
		int cursoIndex = scanner.nextInt() - 1;
		scanner.nextLine();
		scanner.nextLine();

		if (cursoIndex >= 0 && cursoIndex < cursos.size()) {
			Curso curso = cursos.get(cursoIndex);
			System.out.print("Título do módulo: ");
			String tituloModulo = scanner.nextLine();

			Modulo modulo = new Modulo(tituloModulo, new ArrayList<>());
			curso.adicionarModulo(modulo);
			System.out.println("Módulo adicionado ao curso com sucesso!");
		} else {
			System.out.println("Índice de curso inválido.");
		}
	}

	private static void menuPagamento() throws SQLException {
		while (true) {
			System.out.println("\n--- Menu Pagamento ---");
			System.out.println("1 - Criar Pagamento");
			System.out.println("2 - Listar Pagamentos");
			System.out.println("3 - Atualizar Pagamento");
			System.out.println("4 - Deletar Pagamento");
			System.out.println("0 - Voltar");
			System.out.print("Escolha uma opção: ");
			int opcao = scanner.nextInt();
			scanner.nextLine();

			switch (opcao) {
			case 1:
				criarPagamento();
				break;
			case 2:
				listarPagamentos();
				break;
			case 3:
				atualizarPagamento();
				break;
			case 4:
				deletarPagamento();
				break;
			case 0:
				return;
			default:
				System.out.println("Opção inválida. Tente novamente.");
			}
		}
	}

	private static void criarPagamento() throws SQLException {
		System.out.print("Tipo de Pagamento (1 - Cartão, 2 - Pix): ");
		int tipo = scanner.nextInt();
		scanner.nextLine();

		if (tipo != 1 && tipo != 2) {
			System.out.println("Tipo de pagamento inválido.");
			return;
		}

		System.out.print("Valor do pagamento: ");
		double valor = scanner.nextDouble();
		scanner.nextLine();
		System.out.print("Data de Vencimento (AAAA-MM-DD): ");
		String dataVencimento = scanner.nextLine();

		Aluno aluno = selecionarAluno();
		if (aluno == null)
			return;
		Curso curso = selecionarCurso();
		if (curso == null)
			return;

		Pagamento pagamento;
		if (tipo == 1) {
			System.out.print("Número do Cartão: ");
			String numeroCartao = scanner.nextLine();
			System.out.print("Nome do Titular do Cartão: ");
			String nomeTitular = scanner.nextLine();
			System.out.print("Data de Validade do Cartão (MM/AAAA): ");
			String dataValidade = scanner.nextLine();
			System.out.print("CVV do Cartão: ");
			String cvv = scanner.nextLine();
			pagamento = PagamentoCartao.builder().valor(BigDecimal.valueOf(valor)).aluno(aluno).curso(curso)
					.status(PagamentoStatus.PAGO).bandeira("MasterCard").numeroCartao(numeroCartao)
					.nomeTitular(nomeTitular).dataValidade(dataValidade).cvv(cvv).build();
		} else {
			System.out.print("Tempo de Expiração (em segundos): ");
			int tempoExpiracao = scanner.nextInt();
			scanner.nextLine();
			System.out.print("Chave Pix: ");
			String chavePix = scanner.nextLine();
			pagamento = PagamentoPix.builder().valor(BigDecimal.valueOf(valor))
					.dataVencimento(LocalDate.parse(dataVencimento)).aluno(aluno).curso(curso)
					.tempoExpiracao(tempoExpiracao).chavePix(chavePix).build();
		}

		pagamentos.add(pagamento);
		System.out.println("Pagamento criado com sucesso!");
	}

	private static void listarPagamentos() {
		if (pagamentos.isEmpty()) {
			System.out.println("Não há pagamentos cadastrados.");
			return;
		}
		System.out.println("\n--- Lista de Pagamentos ---");
		for (int i = 0; i < pagamentos.size(); i++) {
			Pagamento pagamento = pagamentos.get(i);
			System.out.println((i + 1) + ". " + pagamento.toString());
		}
	}

	private static void atualizarPagamento() {
		listarPagamentos();
		if (pagamentos.isEmpty()) {
			return;
		}
		System.out.print("Digite o número do pagamento que deseja atualizar: ");
		int indice = scanner.nextInt() - 1;
		scanner.nextLine();

		if (indice >= 0 && indice < pagamentos.size()) {
			Pagamento pagamento = pagamentos.get(indice);

			System.out.print("Novo valor do pagamento (ou deixe em branco para manter o mesmo): ");
			if (scanner.hasNextDouble()) {
				double novoValor = scanner.nextDouble();
				scanner.nextLine();
				pagamento.setValor(BigDecimal.valueOf(novoValor));
			}

			System.out.print("Nova data de vencimento (AAAA-MM-DD) (ou deixe em branco para manter a mesma): ");
			String novaDataVencimento = scanner.nextLine();

			if (!novaDataVencimento.isEmpty()) {
				pagamento.setDataVencimento(LocalDate.parse(novaDataVencimento));
			}

			if (pagamento instanceof PagamentoCartao) {
				System.out.print("Novo número do cartão (ou deixe em branco para manter o mesmo): ");
				String novoNumeroCartao = scanner.nextLine();
				if (!novoNumeroCartao.isEmpty()) {
					((PagamentoCartao) pagamento).setNumeroCartao(novoNumeroCartao);
				}
				System.out.print("Novo nome do titular do cartão (ou deixe em branco para manter o mesmo): ");
				String novoNomeTitular = scanner.nextLine();
				if (!novoNomeTitular.isEmpty()) {
					((PagamentoCartao) pagamento).setNomeTitular(novoNomeTitular);
				}
				System.out
						.print("Nova data de validade do cartão (MM/AAAA) (ou deixe em branco para manter a mesma): ");
				String novaDataValidade = scanner.nextLine();
				if (!novaDataValidade.isEmpty()) {
					((PagamentoCartao) pagamento).setDataValidade(novaDataValidade);
				}
				System.out.print("Novo CVV do cartão (ou deixe em branco para manter o mesmo): ");
				String novoCvv = scanner.nextLine();
				if (!novoCvv.isEmpty()) {
					((PagamentoCartao) pagamento).setCvv(novoCvv);
				}
			} else if (pagamento instanceof PagamentoPix) {
				System.out.print("Novo tempo de expiração (em segundos) (ou deixe em branco para manter o mesmo): ");
				if (scanner.hasNextInt()) {
					int novoTempoExpiracao = scanner.nextInt();
					scanner.nextLine();
					((PagamentoPix) pagamento).setTempoExpiracao(novoTempoExpiracao);
				}
				System.out.print("Nova Chave Pix (ou deixe em branco para manter o mesmo): ");
				String novaChavePix = scanner.nextLine();
				if (!novaChavePix.isEmpty()) {
					((PagamentoPix) pagamento).setChavePix(novaChavePix);
				}
			}
			System.out.println("Pagamento atualizado com sucesso!");
		} else {
			System.out.println("Índice inválido.");
		}
	}

	private static void deletarPagamento() {
		listarPagamentos();
		if (pagamentos.isEmpty()) {
			return;
		}
		System.out.print("Digite o número do pagamento que deseja deletar: ");
		int indice = scanner.nextInt() - 1;
		scanner.nextLine();

		if (indice >= 0 && indice < pagamentos.size()) {
			pagamentos.remove(indice);
			System.out.println("Pagamento deletado com sucesso!");
		} else {
			System.out.println("Índice inválido.");
		}
	}

	private static Aluno selecionarAluno() {
		alunos = alunoDao.buscarTodos();
		if (alunos.isEmpty()) {
			System.out.println("Não há alunos cadastrados.");
			return null;
		}
		System.out.println("\n--- Lista de Alunos ---");
		for (int i = 0; i < alunos.size(); i++) {
			Aluno aluno = alunos.get(i);
			System.out.println("ID: " + aluno.getId() + ". Nome: " + aluno.getNome());
		}
		System.out.print("Digite o id do aluno: ");
		Long alunoId = scanner.nextLong();
		scanner.nextLine();
		
		return alunoDao.buscarPorId(alunoId);
	}

	private static Curso selecionarCurso() throws SQLException {
		cursos = cursoDao.buscarTodos();
		if (cursos.isEmpty()) {
			System.out.println("Não há cursos cadastrados.");
			return null;
		}
		System.out.println("\n--- Lista de Cursos ---");
		for (int i = 0; i < cursos.size(); i++) {
			Curso curso = cursos.get(i);
			System.out.println("ID: " + curso.getId() + "  Título: " + curso.getTitulo());
		}
		System.out.print("Digite o id do curso: ");
		Long cursoId = scanner.nextLong();
		scanner.nextLine();
		
		return cursoDao.buscarPorId(cursoId);
	}
	
	private static void menuModulo() throws SQLException {
		while (true) {
			System.out.println("\n--- Menu Módulo ---");
			System.out.println("1 - Criar Módulo");
			System.out.println("2 - Listar Módulos");
			System.out.println("3 - Atualizar Móduulo");
			System.out.println("4 - Deletar Módulo");
			System.out.println("5 - Adicionar Módulo ao Curso");
			System.out.println("0 - Voltar");
			System.out.print("Escolha uma opção: ");
			int opcao = scanner.nextInt();
			scanner.nextLine();

			switch (opcao) {
			case 1:
				criarModulo();
				break;
			case 2:
				listaModulos();
				break;
			case 3:
				atualizarModulo();
				break;
			case 4:
				deletarPagamento();
				break;
			case 5:
				adicionarModuloAoCurso();
				break;
			case 0:
				return;
			default:
				System.out.println("Opção inválida. Tente novamente.");
			}
		}
	}

	private static void menuAula() throws SQLException {
		while (true) {
			System.out.println("\n--- Menu Aula ---");
			System.out.println("1 - Criar Aula");
			System.out.println("2 - Listar Aulas");
			System.out.println("3 - Atualizar Aula");
			System.out.println("4 - Deletar Aula");
			System.out.println("0 - Voltar");
			System.out.print("Escolha uma opção: ");
			int opcao = scanner.nextInt();
			scanner.nextLine();

			switch (opcao) {
			case 1:
				criarModulo();
				break;
			case 2:
				listaModulos();
				break;
			case 3:
				atualizarModulo();
				break;
			case 4:
				deletarPagamento();
				break;
			case 0:
				return;
			default:
				System.out.println("Opção inválida. Tente novamente.");
			}
		}
	}
	
	private static void criarModulo() throws SQLException {
		System.out.print("Título do Módulo: ");
		String titulo = scanner.nextLine();

		Modulo modulo = new Modulo(titulo);
		moduloDao.salvar(modulo);
		
		System.out.println("Modulo criado com sucesso!");
	}
	
	private static void listaModulos() throws SQLException {
		List<Modulo> modulos = moduloDao.listarTodos();
		if (modulos.isEmpty()) {
			System.out.println("Não há modulos cadastrados.");
			return;
		}
		System.out.println("\n--- Lista de Modulos ---");
		for (int i = 0; i < modulos.size(); i++) {
			Modulo modulo = modulos.get(i);
			System.out.println((i + 1) + ". Título: " + modulo.getTitulo());
		}
	}
	
	private static void atualizarModulo() {
		
	}
 }
