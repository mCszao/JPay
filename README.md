# ⚙️ JPay - Backend

Este repositório contém o **backend da aplicação JPay**, desenvolvido em **Java com Spring Boot**.  
Ele é responsável por expor as APIs REST, permitindo o gerenciamento de **contas a pagar, contas bancárias e categorias**.

---

## 🚀 Tecnologias Utilizadas

- **Java 17+**  
- **Spring Boot**  
  - Spring Web  
  - Spring Data JPA  
  - Spring Validation  
- **Banco de Dados**: H2 (em memória, para desenvolvimento)  
- **Lombok**  
- **OpenAPI / Swagger** (via `springdoc-openapi`)  
- **Ferramenta de Build**: Maven  

---

## ▶️ Como Rodar Localmente

1. Clone este repositório:
   ```bash
   git clone https://github.com/mCszao/JPay.git
````

2. Acesse a pasta do projeto:

   ```bash
   cd JPay

3. Compile o projeto com Maven:

   ```bash
   mvn clean install
   ```

4. Rode a aplicação:

   ```bash
   mvn spring-boot:run
   ```

5. Acesse a API localmente:

   ```
   http://localhost:8080
   ```

6. Acesse a documentação da API (Swagger UI):

   ```
   http://localhost:8080/swagger-ui.html
   ```

---

## 📂 Estrutura do Projeto

```bash
src/
 ├── main/
 │   ├── java/com/challenge/JPay/
 │   │   ├── config/
 │   │   │   ├── CorsConfig.java
 │   │   │   ├── DataLoader.java
 │   │   │   └── GlobalCustomExceptionHandler.java
 │   │   │
 │   │   ├── controller/
 │   │   │   ├── AccountPayableController.java
 │   │   │   ├── BankAccountController.java
 │   │   │   └── CategoryController.java
 │   │   │
 │   │   ├── dto/
 │   │   │   ├── request/
 │   │   │   │   ├── AccountPayableRequestDTO.java
 │   │   │   │   ├── BankAccountRequestDTO.java
 │   │   │   │   ├── CategoryRequestDTO.java
 │   │   │   │   └── PaymentRequestDTO.java
 │   │   │   │
 │   │   │   └── response/
 │   │   │       ├── AccountPayableResponseDTO.java
 │   │   │       ├── BankAccountResponseDTO.java
 │   │   │       └── CategoryResponseDTO.java
 │   │   │
 │   │   ├── exception/
 │   │   │   └── (Exceções customizadas e handlers)
 │   │   │
 │   │   ├── model/
 │   │   │   └── (Entidades do domínio)
 │   │   │
 │   │   ├── repository/
 │   │   │   ├── AccountPayableRepository.java
 │   │   │   ├── BankAccountRepository.java
 │   │   │   └── CategoryRepository.java
 │   │   │
 │   │   ├── service/
 │   │   │   ├── AccountPayableService.java
 │   │   │   ├── BankAccountService.java
 │   │   │   └── CategoryService.java
 │   │   │
 │   │   └── JPayApplication.java
 │   │
 │   └── resources/
 │       ├── application.properties
 │
 └── test/
     └── (Testes unitários e de integração)
```

### Organização

* **config/** → Configurações globais (CORS, tratamento de exceções).
* **controller/** → Endpoints REST da aplicação.
* **dto/request** → Objetos de entrada (valores recebidos via API).
* **dto/response** → Objetos de saída (valores retornados pela API).
* **exception/** → Classes de exceções e tratadores globais.
* **model/** → Entidades JPA (mapeamento com o banco).
* **repository/** → Interfaces que estendem `JpaRepository` para persistência.
* **service/** → Contém a lógica de negócios.

---

## 📖 Documentação da API

Após iniciar a aplicação, a documentação estará disponível em:

* Swagger UI:

  ```
  http://localhost:8080/swagger-ui.html
  ```

* OpenAPI JSON:

  ```
  http://localhost:8080/v3/api-docs
  ```

---

## 📌 Observação

Este projeto faz parte do desafio **JPay**, e deve ser utilizado em conjunto com o frontend [JPayFront](https://github.com/mCszao/JPay-front).
