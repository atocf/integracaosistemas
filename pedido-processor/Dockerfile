# Estágio de build
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app

# Copiar o pom.xml e baixar as dependências
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiar o código-fonte e construir o aplicativo
COPY src ./src
RUN mvn clean
RUN mvn clean package -DskipTests

# Estágio final
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copiar o JAR do estágio de build
COPY --from=build /app/target/*.jar app.jar

# Criar diretórios necessários
RUN mkdir -p /app/upload_files/processing && mkdir -p /app/upload_files/completed

# Expor a porta da aplicação
EXPOSE 9080

# Comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]