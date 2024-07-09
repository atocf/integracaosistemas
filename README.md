# LuizaLabs - Desafio Técnico - Vertical Logística

Este projeto consiste em um sistema backend desenvolvido para solucionar o desafio técnico proposto pela LuizaLabs para a Vertical de Logística. O sistema tem como objetivo realizar a leitura, processamento e normalização de um arquivo de pedidos desnormalizado, fornecido em um formato específico, e disponibilizá-lo através de uma API REST.

## Requisitos
Para executar o projeto localmente, você precisará ter instalado:

- Docker: https://docs.docker.com/get-docker/
- Java 17: https://www.oracle.com/java/technologies/downloads/#java17

O sistema foi desenvolvido utilizando as seguintes tecnologias:

- Linguagem de Programação: Java
- Framework: Spring Boot
- Gerenciador de Dependências: Maven
- Banco de Dados: PostgreSQL
- Mensageria: RabbitMQ
- Contêinerização: Docker

## Execução do Projeto
Para executar o projeto localmente, siga as etapas abaixo:

1. Clone o repositório:
```bash    
git clone https://github.com/atocf/integracaosistemas
```

2. Acesse o diretório do projeto:
```bash    
cd ./integracaosistemas
```

3. Suba os containers Docker:
```bash 
docker-compose up -d
```
ou
```bash 
docker-compose up --build
```
4. Acesse a documentação da API:

Após a inicialização completa dos serviços, a documentação da API estará disponível em:

http://localhost:9090/swagger-ui/index.html

5. Acessar o rabbitMQ

http://localhost:15672

7. Acessar WebHook

Para validar se o sistema esta enviando corretamente os dados para o proximo sistema criei um WebHook para validação.

Basta acessar http://localhost:8080 e após o processamento do arquivo clicar em "Atualizar" que o resultado será exibido na tela. 

8. Parar os containers Docker:
```bash 
docker-compose down
```
## Detalhes da Implementação
### API REST: 
A API foi construída utilizando o Spring Boot, e oferece endpoints para o envio do arquivo de pedidos e consulta dos pedidos processados.

### Processamento do Arquivo: 
O sistema realiza a leitura do arquivo, conversão dos dados para o formato JSON normalizado e persistência dos dados.

### Banco de Dados: 
O PostgreSQL é utilizado para armazenar os pedidos processados, permitindo a consulta e filtragem.

### Mensageria: 
O RabbitMQ é utilizado para processamento assíncrono dos pedidos, desacoplando a API do processamento do arquivo.

### Considerações
O projeto foi desenvolvido com foco em simplicidade, legibilidade e manutenibilidade do código, aplicando princípios SOLID e boas práticas de desenvolvimento. A estrutura do projeto e a escolha das tecnologias foram feitas visando atender aos requisitos do desafio e proporcionar uma solução eficiente e escalável.