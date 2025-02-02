package com.test.demo.controllers.SchemaController;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.Map;

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
import com.test.demo.entities.databasePermission.DatabasePermission;
import com.test.demo.entities.user.User;
import com.test.demo.repositories.databasePermissionRepository.DatabasePermissionRepository;
import com.test.demo.repositories.userRepository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class SchemaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DatabasePermissionRepository permissionRepository;

    private final User plainUser = new User();

    private final DatabasePermission permission = new DatabasePermission();

    @BeforeEach
    public void init() {
        plainUser.setName("plainUser");
        plainUser.setEmail("plainUser@example.com");
        plainUser.setPassword(passwordEncoder.encode("@Password123")); // Kodowanie hasła
        plainUser.setRole("USER");
        userRepository.save(plainUser);

        permission.setDbHost("localhost");
        permission.setDbName("DatabaseSchema");
        permission.setDbPort("5432");
        permission.setDbUser("postgres");
        permission.setUser(plainUser);
        permissionRepository.save(permission);
    
    }

    @AfterEach
    public void tearDown() {
        permissionRepository.delete(permission);
        userRepository.delete(plainUser);
    }


    @Test
    @WithMockUser(username = "test@example.com", roles = {"ADMIN"})
    public void testSchemaAdmin() throws Exception{

        String content = String.format("""
                        {
                            "user": "postgres",
                            "host": "localhost",
                            "database": "DatabaseSchema",
                            "password": "QWERTY123",
                            "port": "5432"
                        }
                        """);

        MvcResult result = mockMvc.perform(post("/api/database-schema")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
                .with(csrf())) 
                .andExpect(status().isOk()) // Sprawdź, czy status HTTP to 200 OK
                .andReturn();

        // Sprawdź odpowiedź
        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);

        Map<String, Object> responseMap = new ObjectMapper().readValue(responseContent, new TypeReference<>() {});

        String message = responseMap.get("message").toString();
        assertNotNull(message);
        assertEquals("Schema generating Successful", message);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"ADMIN"})
    public void testSchemaWrongUser() throws Exception{

        String content = String.format("""
                        {
                            "user": "wrongUser",
                            "host": "localhost",
                            "database": "DatabaseSchema",
                            "password": "QWERTY123",
                            "port": "5432"
                        }
                        """);

        MvcResult result = mockMvc.perform(post("/api/database-schema")
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
        assertEquals("Database user authorization failed. Check your user data and try again.", message);
    }

    @Test
    @WithMockUser(username = "plainUser@example.com", roles = {"USER"})
    public void testSchemaRoleUser() throws Exception{

        String content = String.format("""
                        {
                            "user": "postgres",
                            "host": "localhost",
                            "database": "DatabaseSchema",
                            "password": "QWERTY123",
                            "port": "5432",
                            "userId": "%s"
                        }
                        """, plainUser.getId().toString());

        MvcResult result = mockMvc.perform(post("/api/database-schema")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
                .with(csrf())) 
                .andExpect(status().isOk()) // Sprawdź, czy status HTTP to 200 OK
                .andReturn();

        // Sprawdź odpowiedź
        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);

        Map<String, Object> responseMap = new ObjectMapper().readValue(responseContent, new TypeReference<>() {});

        String message = responseMap.get("message").toString();
        assertNotNull(message);
        assertEquals("Schema generating Successful", message);
    }

    @Test
    @WithMockUser(username = "plainUser@example.com", roles = {"USER"})
    public void testSchemaNoPermission() throws Exception{

        String content = String.format("""
                        {
                            "user": "postgres",
                            "host": "localhost",
                            "database": "app",
                            "password": "QWERTY123",
                            "port": "5432",
                            "userId": "%s"
                        }
                        """, plainUser.getId().toString());

        MvcResult result = mockMvc.perform(post("/api/database-schema")
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
        assertEquals("User does not have permission to access this database.", message);
    }

}
