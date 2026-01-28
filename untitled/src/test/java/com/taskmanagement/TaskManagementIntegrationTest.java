package com.taskmanagement;

import com.taskmanagement.dto.CreateUserRequest;
import com.taskmanagement.dto.AuthRequest;
import com.taskmanagement.entity.User;
import com.taskmanagement.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;
    private Long userId;

    @BeforeEach
    public void setUp() throws Exception {
        userRepository.deleteAll();

        // Create a test user
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("password123")
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .build();

        MvcResult registerResult = mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String registerResponse = registerResult.getResponse().getContentAsString();
        userId = objectMapper.readTree(registerResponse).get("id").asLong();

        // Login to get JWT token
        AuthRequest authRequest = new AuthRequest("john@example.com", "password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        jwtToken = objectMapper.readTree(loginResponse).get("token").asText();
    }

    @Test
    public void testRegisterUser() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@example.com")
                .password("password456")
                .dateOfBirth(LocalDate.of(1992, 5, 20))
                .build();

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("jane@example.com"));
    }

    @Test
    public void testGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").exists());
    }

    @Test
    public void testGetUserById() throws Exception {
        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    public void testSearchUsersByName() throws Exception {
        mockMvc.perform(get("/api/users/search/John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("John"));
    }

    @Test
    public void testCreateTask() throws Exception {
        String taskJson = """
                {
                    "title": "Test Task",
                    "description": "This is a test task",
                    "dueDate": "2024-02-01",
                    "status": "TODO"
                }
                """;

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskJson)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    public void testGetAllTasks() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk());
    }

    @Test
    public void testCreateTaskWithoutAuth() throws Exception {
        String taskJson = """
                {
                    "title": "Test Task",
                    "description": "This is a test task",
                    "dueDate": "2024-02-01",
                    "status": "TODO"
                }
                """;

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testLoginSuccess() throws Exception {
        AuthRequest authRequest = new AuthRequest("john@example.com", "password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    public void testLoginFailure() throws Exception {
        AuthRequest authRequest = new AuthRequest("john@example.com", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUpdateUserWithAuth() throws Exception {
        String updateJson = """
                {
                    "firstName": "UpdatedJohn",
                    "lastName": "UpdatedDoe",
                    "email": "updated@example.com",
                    "dateOfBirth": "1990-01-15"
                }
                """;

        mockMvc.perform(put("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("UpdatedJohn"));
    }

    @Test
    public void testDeleteUserWithAuth() throws Exception {
        mockMvc.perform(delete("/api/users/" + userId)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
    }
}

