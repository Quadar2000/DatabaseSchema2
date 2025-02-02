package com.test.demo.controllers.AuthController;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.demo.entities.user.User;
import com.test.demo.entities.user.UserDTO;
import com.test.demo.repositories.userRepository.UserRepository;
import com.test.demo.services.userService.UserService;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserService userService;

    private final User user = new User();

    @BeforeEach
    public void init() {
        user.setName("test");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("@Password123")); // Kodowanie hasła
        user.setRole("ADMIN");
        userRepository.save(user); // Zapisanie użytkownika w bazie danych
    
    }

    @AfterEach
    public void tearDown() {
        userRepository.delete(user);
    }

    @Test
    public void testSuccessfulLogin() throws Exception {

        // Wykonaj żądanie POST do endpointu logowania
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "test@example.com",
                            "password": "@Password123"
                        }
                        """)
                .with(csrf())) // Dodaje token CSRF do żądania
                .andExpect(status().isOk()) // Sprawdź, czy status HTTP to 200 OK
                .andReturn();

        // Sprawdź odpowiedź
        String responseContent = result.getResponse().getContentAsString();
        assertEquals("Authenticated", responseContent);

        // Sprawdź, czy sesja została utworzona
        String sessionId = result.getRequest().getSession().getId();
        assertNotNull(sessionId);
        System.out.println("Session ID: " + sessionId);
    }

    @Test
    public void testFailedLogin() throws Exception {
        // Wykonaj żądanie POST z niepoprawnymi danymi logowania
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "invalid",
                            "password": "wrongpassword"
                        }
                        """)
                .with(csrf())) // Dodaje token CSRF do żądania
                .andExpect(status().isUnauthorized()) // Sprawdź, czy status HTTP to 401 Unauthorized
                .andExpect(content().string("Invalid username or password")); // Sprawdź treść odpowiedzi
    }

    @Test
    public void testCsrfTokenIncludedInRequest() throws Exception {
        // Pobierz token CSRF
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "test@example.com",
                            "password": "@Password123"
                        }
                        """)
                .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        // Sprawdź, czy token CSRF został wygenerowany
        String csrfToken = result.getRequest().getAttribute("_csrf").toString();
        assertNotNull(csrfToken);
    }

    @Test
    public void testLoginWithoutCsrfToken() throws Exception {
        // Wykonaj żądanie POST bez tokenu CSRF
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "test@example.com",
                            "password": "@Password123"
                        }
                        """))
                .andExpect(status().isForbidden()); // Sprawdź, czy status HTTP to 403 Forbidden
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"ADMIN"})
    public void testSuccessfulSessionInfo() throws Exception {

        UserDTO mockUser = new UserDTO("123","test@example.com","test");
        
        when(userService.getUserByEmail("test@example.com")).thenReturn(mockUser);
                
        MvcResult sessionInfoResult = mockMvc.perform(get("/api/auth/session-info")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Sprawdź, czy status HTTP to 200 OK
                .andReturn();

        String responseContent = sessionInfoResult.getResponse().getContentAsString();
        assertNotNull(responseContent);

        Map<String, Object> responseMap = new ObjectMapper().readValue(responseContent, new TypeReference<>() {});
        assertNotNull(responseMap.get("user"));
        assertNotNull(responseMap.get("id"));
        assertNotNull(responseMap.get("roles"));
        assertNotNull(responseMap.get("sessionId"));
                     
    }

    @Test
    public void testFailedSessionInfo() throws Exception {
                
        mockMvc.perform(get("/api/auth/session-info")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()) // Sprawdź, czy status HTTP to 200 OK
                .andReturn();
                           
    }

}
