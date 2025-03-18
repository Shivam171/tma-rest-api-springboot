# Task Management App (Rest API)

Task Management App is a simple task management app that allows users to create, read, update, and delete tasks.

`Note`: In `src/main
/resources` directory there is a `application.properties.example` file, rename it to `application.properties` and configure before running the application.

### 🔥 Features

- Create, read, update, and delete tasks
- Pagination, sorting and searching tasks
- Best practices for REST API development
- Modular architecture (Controller, Service, Repository, etc.)

### ⚙️ Technologies

- Spring Boot
- Java

### 📝 Dependencies

- Spring Web
- Spring Data JPA
- Spring Validation
- Spring MySQL Connector
- Spring Lombok
- Spring Test
- OpenAPI Swagger
- Lombok

### 📃 Swagger API Documentation

- 🔗 Swagger UI: http://localhost:8080/swagger-ui.html
- 🔗 OpenAPI JSON: http://localhost:8080/v3/api-docs

### 🚀 How to Run

1. Clone the repository
2. Navigate to the project directory
3. Run the application using `mvn spring-boot:run`
4. Access the Swagger UI at http://localhost:8080/swagger-ui.html

### 🦈 Docker

1. Maven clean package
    ```
    mvn clean package -DskipTests
   ```
`Note`:
- This will clear the target directory and compile the project. 
- However, if you don't see the target directory, check the `.dockerignore` file. 
- Remove the `target` directory from the `.dockerignore` file if you see it. 
- Then again run the command.

2. Build the Docker image and run the container
    ```
   docker-compose up --build -d
   ```
3. Access the Swagger UI at http://localhost:8080/swagger-ui.html
   

### 💁🏻‍♂️ Things to Improve

- Add security (JWT, OAuth, etc.)
- Add unit tests
- Add integration tests
- Add caching (Redis, Memcached, etc.)
- Implement CI/CD