# Usando a imagem oficial do OpenJDK 17
FROM openjdk:17-jdk-slim

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Copia o JAR gerado pelo Maven para dentro do container
COPY target/jdbc_example-0.0.15-SNAPSHOT.jar app.jar

# Comando para rodar a aplicação
CMD ["java", "-jar", "app.jar"]