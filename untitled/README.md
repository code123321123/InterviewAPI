# Task Management API

A RESTful API service for a simple task management system built with Spring Boot. This application allows users to create, retrieve, update, and delete tasks with proper authentication and authorization.

## Features

- **User Management**
  - User registration
  - User profile retrieval
  - User profile update (authenticated users only)
  - User deletion (authenticated users only)
  - Search users by name

- **Task Management**
  - Create tasks (authenticated users only)
  - Retrieve all tasks
  - Retrieve task by ID
  - Update tasks (authenticated users only, can only update their own)
  - Delete tasks (authenticated users only, can only delete their own)
  - List all tasks
  - Filter tasks by user

- **Authentication & Authorization**
  - JWT-based authentication
  - Secure password encryption with BCrypt
  - Role-based access control
  - Token-based session management

## Technologies Used

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: H2 (In-memory)
- **Build Tool**: Maven
- **Security**: Spring Security 6 with JWT
- **ORM**: JPA/Hibernate
- **Validation**: Jakarta Bean Validation
- **JSON Processing**: Jackson
- **Password Encoding**: BCrypt

## Project Structure

```
src/
├── main/
│   ├── java/com/taskmanagement/
│   │   ├── TaskManagementApplication.java
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── UserController.java
│   │   │   └── TaskController.java
│   │   ├── service/
│   │   │   ├── AuthService.java
│   │   │   ├── UserService.java
│   │   │   └── TaskService.java
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   ├── Task.java
│   │   │   └── TaskStatus.java
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   └── TaskRepository.java
│   │   ├── dto/
│   │   │   ├── UserDTO.java
│   │   │   ├── CreateUserRequest.java
│   │   │   ├── UpdateUserRequest.java
│   │   │   ├── TaskDTO.java
│   │   │   ├── CreateTaskRequest.java
│   │   │   ├── UpdateTaskRequest.java
│   │   │   ├── AuthRequest.java
│   │   │   └── AuthResponse.java
│   │   ├── security/
│   │   │   ├── JwtTokenProvider.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   ├── UserPrincipal.java
│   │   │   └── CustomUserDetailsService.java
│   │   ├── config/
│   │   │   └── SecurityConfig.java
│   │   └── exception/
│   │       ├── ResourceNotFoundException.java
│   │       ├── UnauthorizedException.java
│   │       ├── EmailAlreadyExistsException.java
│   │       ├── ErrorResponse.java
│   │       ├── ValidationErrorResponse.java
│   │       └── GlobalExceptionHandler.java
│   └── resources/
│       └── application.properties
└── pom.xml
```

## Entity Models

### User Entity
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "dateOfBirth": "1990-01-15",
  "createdAt": "2024-01-15",
  "updatedAt": "2024-01-15"
}
```

### Task Entity
```json
{
  "id": 1,
  "title": "Complete project",
  "description": "Finish the Spring Boot API",
  "dueDate": "2024-02-01",
  "status": "IN_PROGRESS",
  "userId": 1,
  "createdAt": "2024-01-15",
  "updatedAt": "2024-01-15"
}
```

## Task Status Values
- `TODO` - Task not yet started
- `IN_PROGRESS` - Task is being worked on
- `DONE` - Task is completed

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Git

## Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/task-management-api.git
cd task-management-api
```

### 2. Build the Project

```bash
mvn clean install
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

Or using Java directly:

```bash
java -jar target/task-management-api-1.0.0.jar
```

The application will start on `http://localhost:8080`

## API Endpoints

### Authentication Endpoints

#### Login
```
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}

Response: 201 Created
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "userId": 1,
  "email": "john@example.com"
}
```

### User Endpoints

#### Register New User
```
POST /api/users/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "password123",
  "dateOfBirth": "1990-01-15"
}

Response: 201 Created
```

#### Get All Users
```
GET /api/users
Response: 200 OK
[
  {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "dateOfBirth": "1990-01-15",
    "createdAt": "2024-01-15",
    "updatedAt": "2024-01-15"
  }
]
```

#### Get User by ID
```
GET /api/users/{id}
Response: 200 OK
```

#### Search Users by Name
```
GET /api/users/search/{name}
Response: 200 OK
```

#### Update User (Authenticated - own profile only)
```
PUT /api/users/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane@example.com",
  "dateOfBirth": "1990-01-15"
}

Response: 200 OK
```

#### Delete User (Authenticated - own profile only)
```
DELETE /api/users/{id}
Authorization: Bearer {token}
Response: 204 No Content
```

### Task Endpoints

#### Create Task (Authenticated)
```
POST /api/tasks
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "Complete project",
  "description": "Finish the Spring Boot API",
  "dueDate": "2024-02-01",
  "status": "TODO"
}

Response: 201 Created
{
  "id": 1,
  "title": "Complete project",
  "description": "Finish the Spring Boot API",
  "dueDate": "2024-02-01",
  "status": "TODO",
  "userId": 1,
  "createdAt": "2024-01-15",
  "updatedAt": "2024-01-15"
}
```

#### Get All Tasks
```
GET /api/tasks
Response: 200 OK
```

#### Get Task by ID
```
GET /api/tasks/{id}
Response: 200 OK
```

#### Get User's Tasks (Authenticated)
```
GET /api/tasks/user/{userId}
Authorization: Bearer {token}
Response: 200 OK
```

#### Update Task (Authenticated - own tasks only)
```
PUT /api/tasks/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "Complete project",
  "description": "Finish the Spring Boot API",
  "dueDate": "2024-02-01",
  "status": "IN_PROGRESS"
}

Response: 200 OK
```

#### Delete Task (Authenticated - own tasks only)
```
DELETE /api/tasks/{id}
Authorization: Bearer {token}
Response: 204 No Content
```

## Using the API with cURL

### 1. Register a User
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "password": "password123",
    "dateOfBirth": "1990-01-15"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

### 3. Create a Task (with token)
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Complete project",
    "description": "Finish the Spring Boot API",
    "dueDate": "2024-02-01",
    "status": "TODO"
  }'
```

### 4. Get All Tasks
```bash
curl -X GET http://localhost:8080/api/tasks
```

### 5. Update User (with token)
```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Doe",
    "email": "jane@example.com",
    "dateOfBirth": "1990-01-15"
  }'
```

## Using the API with Postman

1. **Import Collection**: Create a new collection in Postman
2. **Set Base URL**: `{{base_url}}` = `http://localhost:8080`
3. **Authentication**: Add the JWT token to the Authorization header:
   ```
   Bearer YOUR_JWT_TOKEN
   ```

### Sample Requests in Postman

#### Register User
- Method: POST
- URL: `{{base_url}}/api/users/register`
- Body (JSON):
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "password123",
  "dateOfBirth": "1990-01-15"
}
```

#### Login
- Method: POST
- URL: `{{base_url}}/api/auth/login`
- Body (JSON):
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

## Authentication

This API uses JWT (JSON Web Tokens) for authentication. 

### How to use:
1. Register a new user or login with existing credentials
2. Copy the token from the response
3. Add it to the `Authorization` header in subsequent requests as: `Bearer <token>`
4. The token is valid for 24 hours

## Error Handling

The API returns proper HTTP status codes and error messages:

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: 999",
  "path": "/api/users/999"
}
```

### Common Status Codes:
- `200 OK` - Successful request
- `201 Created` - Resource created successfully
- `204 No Content` - Resource deleted successfully
- `400 Bad Request` - Invalid input/validation error
- `401 Unauthorized` - Missing or invalid authentication
- `403 Forbidden` - User doesn't have permission
- `404 Not Found` - Resource not found
- `409 Conflict` - Email already exists
- `500 Internal Server Error` - Server error

## Database

The application uses an in-memory H2 database by default. The database is recreated on every application restart.

### H2 Console
Access the H2 console at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (leave empty)

## Security Features

1. **Password Encryption**: All passwords are encrypted using BCrypt
2. **JWT Token Authentication**: Stateless authentication using JWT
3. **CORS Support**: Configured to accept requests from any origin
4. **Input Validation**: All inputs are validated before processing
5. **Authorization**: Users can only modify their own data

## Testing

The application includes validation for:
- Required fields
- Email format validation
- Date format validation (YYYY-MM-DD)
- Unique email constraint
- Task status validation (TODO, IN_PROGRESS, DONE)

## Deployment

### Deploy to Production

1. Build the JAR file:
```bash
mvn clean package
```

2. Configure production properties in `application-prod.properties`

3. Run with production profile:
```bash
java -jar target/task-management-api-1.0.0.jar --spring.profiles.active=prod
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues, questions, or suggestions, please open an issue on GitHub.

---

**Last Updated**: January 2024
**Version**: 1.0.0

