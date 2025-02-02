package com.test.demo.controllers.PermissionController;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import com.test.demo.entities.databasePermission.DatabasePermissionDTO;
import com.test.demo.entities.user.User;
import com.test.demo.entities.user.UserDTO;
import com.test.demo.repositories.databasePermissionRepository.DatabasePermissionRepository;
import com.test.demo.repositories.userRepository.UserRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PermissionControllerTest {

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

        Optional<DatabasePermissionDTO> permissionAdded = permissionRepository.findUniqueDatabasePermission(plainUser.getId().toString(), "app2", "localhost");
        if(permissionAdded.isPresent()){

            permissionRepository.deleteDatabasePermission(plainUser.getId().toString(), "app2", "localhost");
        }
        userRepository.delete(plainUser);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    public void testGetPermissions() throws Exception{

        MvcResult result = mockMvc.perform(get("/api/get-permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .param("userId", plainUser.getId().toString()))
                .andExpect(status().isOk()) // Sprawdź, czy status HTTP to 200 OK
                .andReturn();

        // Sprawdź odpowiedź
        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);

        Map<String, Object> responseMap = new ObjectMapper().readValue(responseContent, new TypeReference<>() {});

        ObjectMapper objectMapper = new ObjectMapper();
        UserDTO userResponse = objectMapper.convertValue(responseMap.get("user"), UserDTO.class);
        assertNotNull(userResponse);
        assertEquals(plainUser.getId(), userResponse.getId());

        List <DatabasePermissionDTO> permissions = (List <DatabasePermissionDTO>) responseMap.get("permissions");
        assertNotNull(permissions);
        assertNotEquals(0,permissions.size());
       
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"ADMIN"})
    public void testDeletePermission() throws Exception{

        DatabasePermission permissionDelete = new DatabasePermission();

        permissionDelete.setDbHost("localhost");
        permissionDelete.setDbName("app");
        permissionDelete.setDbPort("5432");
        permissionDelete.setDbUser("postgres");
        permissionDelete.setUser(plainUser);

        permissionRepository.save(permissionDelete);

        MvcResult result = mockMvc.perform(delete("/api/delete-permission")
                .contentType(MediaType.APPLICATION_JSON)
                .param("userId", plainUser.getId().toString())
                .param("host", "localhost")
                .param("name", "app")
                .with(csrf())) // Sprawdź, czy status HTTP to 200 OK
                .andExpect(status().isOk())
                .andReturn();

        // Sprawdź odpowiedź
        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);

        Map<String, Object> responseMap = new ObjectMapper().readValue(responseContent, new TypeReference<>() {});

        String message = responseMap.get("message").toString();
        assertNotNull(message);
        assertEquals("Permission deleted successfully", message);
       
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    public void testDeletePermissionRoleUser() throws Exception{

        MvcResult result = mockMvc.perform(delete("/api/delete-permission")
                .contentType(MediaType.APPLICATION_JSON)
                .param("userId", plainUser.getId().toString())
                .param("host", "localhost")
                .param("name", "DatabaseSchema")
                .with(csrf())) // Sprawdź, czy status HTTP to 200 OK
                .andExpect(status().isForbidden())
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
    public void testGrantPermissionAdmin() throws Exception{

        String content = String.format("""
                        {
                            "user": "postgres",
                            "host": "localhost",
                            "database": "app2",
                            "password": "QWERTY123",
                            "port": "5432",
                            "userId": "%s"
                        }
                        """, plainUser.getId().toString());

        MvcResult result = mockMvc.perform(post("/api/grant-permission")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
                .with(csrf())) // Sprawdź, czy status HTTP to 200 OK
                .andExpect(status().isOk())
                .andReturn();

        // Sprawdź odpowiedź
        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);

        Map<String, Object> responseMap = new ObjectMapper().readValue(responseContent, new TypeReference<>() {});

        String message = responseMap.get("message").toString();
        assertNotNull(message);
        assertEquals("Permission granted successfully", message);
       
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    public void testGrantPermissionRoleUser() throws Exception{

        String content = String.format("""
                        {
                            "user": "postgres",
                            "host": "localhost",
                            "database": "app2",
                            "password": "QWERTY123",
                            "port": "5432",
                            "userId": "%s"
                        }
                        """, plainUser.getId().toString());

        MvcResult result = mockMvc.perform(post("/api/grant-permission")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
                .with(csrf()))
                .andExpect(status().isForbidden())
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
    public void testGrantPermissionWrongdatabase() throws Exception{

        String content = String.format("""
                        {
                            "user": "postgres",
                            "host": "localhost",
                            "database": "wrongDatabase",
                            "password": "QWERTY123",
                            "port": "5432",
                            "userId": "%s"
                        }
                        """, plainUser.getId().toString());

        MvcResult result = mockMvc.perform(post("/api/grant-permission")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andReturn();

        // Sprawdź odpowiedź
        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);

        Map<String, Object> responseMap = new ObjectMapper().readValue(responseContent, new TypeReference<>() {});

        String message = responseMap.get("message").toString();
        assertNotNull(message);
        assertEquals("Database not found. Check your database name and try again.", message);
       
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"ADMIN"})
    public void testGrantPermissionWrongUser() throws Exception{

        String content = String.format("""
                        {
                            "user": "wrongUser",
                            "host": "localhost",
                            "database": "app2",
                            "password": "QWERTY123",
                            "port": "5432",
                            "userId": "%s"
                        }
                        """, plainUser.getId().toString());

        MvcResult result = mockMvc.perform(post("/api/grant-permission")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
                .with(csrf()))
                .andExpect(status().isForbidden())
                .andReturn();

        // Sprawdź odpowiedź
        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);

        Map<String, Object> responseMap = new ObjectMapper().readValue(responseContent, new TypeReference<>() {});

        String message = responseMap.get("message").toString();
        assertNotNull(message);
        assertEquals("Database user authorization failed. Check your user data and try again.", message);
       
    }

}
