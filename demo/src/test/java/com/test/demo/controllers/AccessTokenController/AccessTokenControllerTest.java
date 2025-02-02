package com.test.demo.controllers.AccessTokenController;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.demo.entities.accessToken.AccessToken;
import com.test.demo.entities.databasePermission.DatabasePermission;
import com.test.demo.entities.user.User;
import com.test.demo.repositories.accessTokenRepository.AccessTokenRepository;
import com.test.demo.repositories.databasePermissionRepository.DatabasePermissionRepository;
import com.test.demo.repositories.userRepository.UserRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
public class AccessTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DatabasePermissionRepository permissionRepository;

    @Autowired
    private AccessTokenRepository accessTokenRepository;

    private final User user = new User();

    private final AccessToken accessToken = new AccessToken();

    @BeforeEach
    public void init() {
        user.setName("test");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("@Password123")); // Kodowanie hasła
        user.setRole("ADMIN");
        userRepository.save(user);

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(5);

        accessToken.setUser(user);  // Ustawiamy użytkownika
        accessToken.setToken(token);
        accessToken.setExpiryDate(expiryDate);

        accessTokenRepository.save(accessToken);

    }

    @AfterEach
    public void tearDown() {
        accessTokenRepository.deleteTokensByUserId(user.getId().toString());
        userRepository.delete(user);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"ADMIN"})
    public void testGenerateTokenAdmin() throws Exception {
        String content = String.format("""
                        {
                            "userId": "%s"
                        }
                        """, user.getId().toString());

        MvcResult result = mockMvc.perform(post("/api/generate-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
                .with(csrf())) 
                .andExpect(status().isOk()) // Sprawdź, czy status HTTP to 200 OK
                .andReturn();

        // Sprawdź odpowiedź
        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);

        Map<String, Object> responseMap = new ObjectMapper().readValue(responseContent, new TypeReference<>() {});

        String token= responseMap.get("token").toString();
        assertNotNull(token);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    public void testGenerateTokenRoleUser() throws Exception {
        String content = String.format("""
                        {
                            "userId": "%s"
                        }
                        """, user.getId().toString());

        MvcResult result = mockMvc.perform(post("/api/generate-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
                .with(csrf())) 
                .andExpect(status().isForbidden()) // Sprawdź, czy status HTTP to 200 OK
                .andReturn();

        // Sprawdź odpowiedź
        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);

        Map<String, Object> responseMap = new ObjectMapper().readValue(responseContent, new TypeReference<>() {});

        String message = responseMap.get("message").toString();
        assertNotNull(message);
        assertEquals("You don't have permission to access this resource", message);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"ADMIN"})
    public void testValidateTokenAdmin() throws Exception {

        LocalDateTime expiryDate = LocalDateTime.now().minusMinutes(5);

        accessToken.setExpiryDate(expiryDate);

        accessTokenRepository.save(accessToken);

        String content = String.format("""
                        {
                            "token": "%s"
                        }
                        """, accessToken.getToken());

        MvcResult result = mockMvc.perform(post("/api/validate-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
                .with(csrf())) 
                .andExpect(status().isBadRequest()) // Sprawdź, czy status HTTP to 200 OK
                .andReturn();

        // Sprawdź odpowiedź
        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);

        Map<String, Object> responseMap = new ObjectMapper().readValue(responseContent, new TypeReference<>() {});

        String message = responseMap.get("message").toString();
        assertNotNull(message);
        assertEquals("Invalid or expired token", message);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    public void testValidateTokenRoleUser() throws Exception {

        String content = String.format("""
                        {
                            "token": "%s"
                        }
                        """, accessToken.getToken());

        MvcResult result = mockMvc.perform(post("/api/validate-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
                .with(csrf())) 
                .andExpect(status().isForbidden()) // Sprawdź, czy status HTTP to 200 OK
                .andReturn();

        // Sprawdź odpowiedź
        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);

        Map<String, Object> responseMap = new ObjectMapper().readValue(responseContent, new TypeReference<>() {});

        String message = responseMap.get("message").toString();
        assertNotNull(message);
        assertEquals("You don't have permission to access this resource", message);
    }

}
