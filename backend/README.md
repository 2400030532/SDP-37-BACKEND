# Backend Development Guide

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/easyintern/api/
│   │   │   ├── controller/          # REST API endpoints
│   │   │   ├── model/               # JPA entities
│   │   │   ├── repository/          # Data access layer
│   │   │   ├── service/             # Business logic layer
│   │   │   └── EasyInternApiApplication.java
│   │   └── resources/
│   │       └── application.yml      # Spring Boot config
│   └── test/
│       └── java/                    # Unit/Integration tests
├── pom.xml                          # Maven dependencies
└── Dockerfile                       # Docker configuration
```

## Prerequisites

- **Java 17+**: Download from [oracle.com](https://www.oracle.com/java/technologies/javase-downloads.html) or use [OpenJDK](https://openjdk.org/)
- **Maven 3.8+**: Download from [maven.apache.org](https://maven.apache.org/download.cgi)
- **MySQL 8.0+** (optional, H2 is used by default for development)

## Getting Started

### 1. Setup Java & Maven

Verify installations:
```bash
java -version
mvn -version
```

### 2. Configure Database

#### Option A: Use H2 (Default - Recommended for development)
No configuration needed. H2 runs in-memory and resets on restart.

#### Option B: Use MySQL
Edit `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/easyintern
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
```

Create the database:
```sql
CREATE DATABASE easyintern;
```

### 3. Run the Application

#### Development Mode with auto-reload
```bash
mvn spring-boot:run
```

#### Build and Run JAR
```bash
mvn clean package
java -jar target/easyintern-api-1.0.0.jar
```

The API will be available at: `http://localhost:8080/api`

## Available Endpoints

### Health Check
```
GET http://localhost:8080/api/v1/status/health
```

### Internships API
```
GET    http://localhost:8080/api/v1/internships
GET    http://localhost:8080/api/v1/internships/active
GET    http://localhost:8080/api/v1/internships/{id}
GET    http://localhost:8080/api/v1/internships/search?keyword=keyword
GET    http://localhost:8080/api/v1/internships/company/{company}
POST   http://localhost:8080/api/v1/internships
PUT    http://localhost:8080/api/v1/internships/{id}
DELETE http://localhost:8080/api/v1/internships/{id}
```

### Example Requests

#### Get all internships
```bash
curl http://localhost:8080/api/v1/internships
```

#### Create an internship
```bash
curl -X POST http://localhost:8080/api/v1/internships \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Java Developer Intern",
    "company": "Tech Corp",
    "description": "Join our team as a Java developer",
    "location": "New York, NY",
    "stipend": "$10,000",
    "duration": 3,
    "skills": "Java, Spring Boot, MySQL"
  }'
```

## Project Configuration

### application.yml

#### Server Configuration
```yaml
server:
  port: 8080
  servlet:
    context-path: /api
```

#### Database Configuration
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/easyintern
    username: root
    password: root
  jpa:
    hibernate:
      ddl-auto: update
```

#### CORS Configuration
Update in `EasyInternApiApplication.java`:
```java
registry.addMapping("/api/**")
    .allowedOrigins("http://localhost:3000", "http://localhost:5173")
    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
```

## Development Workflow

### Adding a New Entity

1. **Create Entity** in `model/`:
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String email;
}
```

2. **Create Repository** in `repository/`:
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
```

3. **Create Service** in `service/`:
```java
@Service
public class UserService {
    public User createUser(User user) {
        return userRepository.save(user);
    }
}
```

4. **Create Controller** in `controller/`:
```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    @PostMapping
    public ResponseEntity<User> create(@RequestBody User user) {
        return ResponseEntity.ok(userService.createUser(user));
    }
}
```

### Running Tests

```bash
mvn test
```

### Code Style & Quality

Run checkstyle:
```bash
mvn checkstyle:check
```

## Troubleshooting

### Port 8080 Already in Use
Change the port in `application.yml`:
```yaml
server:
  port: 8081
```

### Database Connection Issues
- Verify MySQL is running: `mysql -u root -p`
- Check credentials in `application.yml`
- Ensure database exists: `CREATE DATABASE easyintern;`

### Build Failures
Clean and rebuild:
```bash
mvn clean install
```

## Docker Support

### Build Docker Image
```bash
docker build -t easyintern-api .
```

### Run Container
```bash
docker run -p 8080:8080 -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/easyintern easyintern-api
```

## Debugging

### Enable Debug Logging
Update `application.yml`:
```yaml
logging:
  level:
    com.easyintern: DEBUG
```

### Run with Debug Mode
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
```

## Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Spring Security](https://spring.io/projects/spring-security)
- [MySQL Documentation](https://dev.mysql.com/doc/)

---

For more help, check the main [README.md](../README.md)
