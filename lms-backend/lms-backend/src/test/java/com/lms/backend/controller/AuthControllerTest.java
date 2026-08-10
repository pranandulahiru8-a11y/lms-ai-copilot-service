package com.lms.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.backend.dto.AuthRequest;
import com.lms.backend.dto.RegisterRequest;
import com.lms.backend.entity.Role;
import com.lms.backend.entity.User;
import com.lms.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/auth/register: Should successfully register a new user with default role")
    void register_Success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Manoj Baba");
        request.setEmail("baba@lms.com");
        request.setPassword("SecurePass123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully!"));

        // Verify DB State & Password Encoding
        User savedUser = userRepository.findByEmail("baba@lms.com").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getRole()).isEqualTo(Role.ROLE_STUDENT);
        assertThat(passwordEncoder.matches("SecurePass123", savedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("POST /api/auth/register: Should fail when registering with an existing email")
    void register_DuplicateEmail_Fails() throws Exception {
        // Save initial user
        User existingUser = User.builder()
                .fullName("Existing User")
                .email("duplicate@lms.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ROLE_STUDENT)
                .build();
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest();
        request.setFullName("New User");
        request.setEmail("duplicate@lms.com");
        request.setPassword("newpassword123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email already in use!"));
    }

    @Test
    @DisplayName("POST /api/auth/login: Should return JWT token and user details on valid credentials")
    void login_Success() throws Exception {
        User user = User.builder()
                .fullName("Test Instructor")
                .email("instructor@lms.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ROLE_INSTRUCTOR)
                .build();
        userRepository.save(user);

        AuthRequest request = new AuthRequest();
        request.setEmail("instructor@lms.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.email").value("instructor@lms.com"))
                .andExpect(jsonPath("$.role").value("ROLE_INSTRUCTOR"));
    }

    @Test
    @DisplayName("POST /api/auth/login: Should return HTTP 400 when password is invalid")
    void login_InvalidPassword_Fails() throws Exception {
        User user = User.builder()
                .fullName("Test User")
                .email("user@lms.com")
                .password(passwordEncoder.encode("correctpass"))
                .role(Role.ROLE_STUDENT)
                .build();
        userRepository.save(user);

        AuthRequest request = new AuthRequest();
        request.setEmail("user@lms.com");
        request.setPassword("wrongpass");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid credentials"));
    }
}