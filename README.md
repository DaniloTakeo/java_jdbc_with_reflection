# Mini ORM - JDBC

Este projeto implementa uma solução de Mini ORM utilizando JDBC puro. O objetivo é criar um repositório genérico que simplifique a interação com o banco de dados sem a necessidade de utilizar frameworks pesados como o Hibernate.

## Funcionalidades

- Implementação da interface `GenericRepository<T, ID>` e da classe abstrata `AbstractRepository<T, ID>`.
- Métodos genéricos para operações de CRUD: `save`, `findAll`, `findById`, `update`, `deleteById`.
- Anotações personalizadas para mapeamento de entidades e campos do banco:
  - `@Table`: Define o nome da tabela no banco de dados.
  - `@Column`: Mapeia um campo da classe para uma coluna da tabela.
  - `@Id`: Marca o campo como chave primária.
  - `@Transient`: Indica que o campo não será persistido no banco de dados.

## Estrutura do Projeto

O projeto possui uma estrutura simples que facilita a adição de novas entidades e repositórios para manipulação de dados.

### Exemplos de Repositórios

- `AlunoRepository`: Implementação do repositório específico para a entidade `Aluno`.
- `CursoRepository`: Implementação do repositório específico para a entidade `Curso`.

### Funcionalidades dos Métodos Genéricos

- `save(T entity)`: Salva uma nova entidade no banco.
- `findAll()`: Recupera todas as entidades de uma tabela.
- `findById(ID id)`: Busca uma entidade pelo seu identificador.
- `update(T entity)`: Atualiza os dados de uma entidade no banco.
- `deleteById(ID id)`: Deleta uma entidade do banco pelo seu identificador.

## Como Usar

### Banco de Dados

O projeto utiliza MySQL para persistência dos dados. Você pode configurar o banco de dados para rodar localmente ou utilizar um container Docker com o `docker-compose`.

#### Usando o Banco Local

1. Crie um banco de dados chamado `plataforma` no MySQL local:
    ```sql
    CREATE DATABASE plataforma;
    ```

2. A classe `DatabaseConnection` está configurada para utilizar a URL:
    ```java
    jdbc:mysql://localhost:3306/plataforma
    ```

3. O projeto irá conectar automaticamente ao banco e realizar as operações de CRUD com base nas classes e suas respectivas anotações.

#### Usando o Docker Compose

1. Na raiz do projeto, execute o seguinte comando para rodar o MySQL via Docker:
    ```bash
    docker-compose up
    ```

2. A URL de conexão no Docker é:
    ```java
    jdbc:mysql://mysql-db:3306/plataforma
    ```

3. O banco de dados será configurado automaticamente pelo Docker.

## Diagramas

### Diagrama de Classe UML

![Diagrama UML](https://github.com/DaniloTakeo/object_orientation_project/blob/main/object-orientation/src/main/resources/diagrama%20de%20classes.png)

### Diagrama DER (Modelo Relacional do Banco de Dados)

![Diagram DER](https://github.com/DaniloTakeo/java_jdbc/blob/main/jdbc_example/src/main/resources/DER.drawio.png)

## Contribuições

Sinta-se à vontade para abrir issues ou enviar pull requests. Qualquer contribuição é bem-vinda!

## 👨‍💻 Autor
Danilo Takeo Kanizawa
