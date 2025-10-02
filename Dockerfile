# Estágio 1: Construção (Build) do projeto KMP
FROM openjdk:17-jdk-slim as builder
WORKDIR /app

# Copia os arquivos de configuração do Gradle da raiz do projeto
COPY settings.gradle.kts build.gradle.kts gradlew ./
COPY gradle ./gradle

# Copia o código-fonte do módulo 'backend'
COPY backend ./backend

# IMPORTANTE: Se o seu módulo 'backend' depende de um módulo 'shared',
# descomente a linha abaixo para copiar o 'shared' também.
# COPY shared ./shared

# Concede permissão de execução para o gradlew
RUN chmod +x ./gradlew

# Executa o build especificamente para o módulo :backend
RUN ./gradlew :backend:bootJar

# Estágio 2: Execução (Runtime) da aplicação final
FROM openjdk:17-jre-slim
WORKDIR /app

# Copia o arquivo .jar do caminho correto, que agora está dentro da pasta do módulo
COPY --from=builder /app/backend/build/libs/*.jar app.jar

# Expõe a porta que o Spring Boot usa por padrão
EXPOSE 8080

# Comando para iniciar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]