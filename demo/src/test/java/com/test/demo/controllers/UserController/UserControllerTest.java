package com.test.demo.controllers.UserController;

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
import com.test.demo.entities.user.User;
import com.test.demo.entities.user.UserDTO;
import com.test.demo.repositories.userRepository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final User user = new User();

    private final User userToDelete = new User();

    @BeforeEach
    public void init() {
        user.setName("test");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("@Password123")); // Kodowanie hasła
        user.setRole("ADMIN");
        userRepository.save(user); // Zapisanie użytkownika w bazie danych

        
        userToDelete.setName("deleteUser");
        userToDelete.setEmail("deleteUser@example.com");
        userToDelete.setPassword(passwordEncoder.encode("@Password123")); // Kodowanie hasła
        userToDelete.setRole("USER");
        userRepository.save(userToDelete);
    
    }

    @AfterEach
    public void tearDown() {
        userRepository.delete(user);
        userRepository.delete(userToDelete);
        Optional<User> testUser= userRepository.findByEmail("test2@example.com");
        if(testUser.isPresent()){
            userRepository.deleteById(testUser.get().getId());
        }
        
    }


    @Test
    @WithMockUser(username = "test@example.com", roles = {"ADMIN"})
    public void testRegisterUserAdmin() throws Exception{

        MvcResult result = mockMvc.perform(post("/api/register-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "test2",
                            "email": "test2@example.com",
                            "password": "@Password123"
                        }
                        """)
                .with(csrf())) // Dodaje token CSRF do żądania
                .andExpect(status().isOk()) // Sprawdź, czy status HTTP to 200 OK
                .andReturn();

        // Sprawdź odpowiedź
        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);

        Map<String, Object> responseMap = new ObjectMapper().readValue(responseContent, new TypeReference<>() {});

        String message = responseMap.get("message").toString();
        assertNotNull(message);
        assertEquals("User created successfully", message);
       
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"ADMIN"})
    public void testRegisterUserAdminBadRequest() throws Exception{

        mockMvc.perform(post("/api/register-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "test2",
                            "email": "wrongEmail",
                            "password": "@Password123"
                        }
                        """)
                .with(csrf())) // Dodaje token CSRF do żądania
                .andExpect(status().isBadRequest()) // Sprawdź, czy status HTTP to 400
                .andReturn();
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"ADMIN"})
    public void testRegisterUserAdminUserExists() throws Exception{

        MvcResult result = mockMvc.perform(post("/api/register-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "test",
                            "email": "test@example.com",
                            "password": "@Password123"
                        }
                        """)
                .with(csrf())) // Dodaje token CSRF do żądania
                .andExpect(status().isBadRequest()) // Sprawdź, czy status HTTP to 400
                .andReturn();

                // Sprawdź odpowiedź
        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);

        Map<String, Object> responseMap = new ObjectMapper().readValue(responseContent, new TypeReference<>() {});

        String message = responseMap.get("message").toString();
        assertNotNull(message);
        assertEquals("Username and email must be unique", message);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    public void testRegisterUserRoleUser() throws Exception{

        MvcResult result = mockMvc.perform(post("/api/register-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "test2",
                            "email": "test2@example.com",
                            "password": "@Password123"
                        }
                        """)
                .with(csrf())) // Dodaje token CSRF do żądania
                .andExpect(status().isForbidden()) // Sprawdź, czy status HTTP to 403
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);

        Map<String, Object> responseMap = new ObjectMapper().readValue(responseContent, new TypeReference<>() {});

        String message = responseMap.get("message").toString();
        assertNotNull(message);
        assertEquals("You don't have permission to access this resource", message);

    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"ADMIN"})
    public void testGetUsersAdmin() throws Exception{

        MvcResult result = mockMvc.perform(get("/api/get-users")
                .contentType(MediaType.APPLICATION_JSON)) 
                .andExpect(status().isOk()) // Sprawdź, czy status HTTP to 200 OK
                .andReturn();

        // Sprawdź odpowiedź
        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);

        Map<String, Object> responseMap = new ObjectMapper().readValue(responseContent, new TypeReference<>() {});

        List <UserDTO> users = (List <UserDTO>) responseMap.get("users");
        assertNotNull(users);
        assertNotEquals(0,users.size());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    public void testGetUsersRoleUser() throws Exception{

        MvcResult result = mockMvc.perform(get("/api/get-users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()) // Sprawdź, czy status HTTP to 403
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
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    public void testGetProfile() throws Exception{

        MvcResult result = mockMvc.perform(get("/api/get-profile")
                .contentType(MediaType.APPLICATION_JSON)
                .param("id",user.getId().toString())) 
                .andExpect(status().isOk()) // Sprawdź, czy status HTTP to 200 OK
                .andReturn();

        // Sprawdź odpowiedź
        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);

        Map<String, Object> responseMap = new ObjectMapper().readValue(responseContent, new TypeReference<>() {});


        ObjectMapper objectMapper = new ObjectMapper();
        UserDTO userResponse = objectMapper.convertValue(responseMap.get("user"), UserDTO.class);
        assertNotNull(userResponse);
        assertEquals(user.getId(), userResponse.getId());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    public void testGetProfileBadId() throws Exception{

        MvcResult result = mockMvc.perform(get("/api/get-profile")
                .contentType(MediaType.APPLICATION_JSON)
                .param("id","123"))
                .andExpect(status().isNotFound())
                .andReturn();

        // Sprawdź odpowiedź
        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);

        Map<String, Object> responseMap = new ObjectMapper().readValue(responseContent, new TypeReference<>() {});

        String message = responseMap.get("message").toString();
        assertNotNull(message);
        assertEquals("User not found", message);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"ADMIN"})
    public void testDeleteUserAdmin() throws Exception{
        

        MvcResult result = mockMvc.perform(delete("/api/delete-user")
                .contentType(MediaType.APPLICATION_JSON)
                .param("id",userToDelete.getId().toString())
                .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        // Sprawdź odpowiedź
        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);
        assertEquals("User deleted successfully", responseContent); 
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"ADMIN"})
    public void testDeleteUserBadId() throws Exception{
        

        MvcResult result = mockMvc.perform(delete("/api/delete-user")
                .contentType(MediaType.APPLICATION_JSON)
                .param("id","123")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andReturn();

       // Sprawdź odpowiedź
       String responseContent = result.getResponse().getContentAsString();
       assertNotNull(responseContent);
       assertEquals("User not found", responseContent);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    public void testDeleteUserRoleUser() throws Exception{
        

        MvcResult result = mockMvc.perform(delete("/api/delete-user")
                .contentType(MediaType.APPLICATION_JSON)
                .param("id",userToDelete.getId().toString())
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
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    public void testChangePassword() throws Exception{
        

        MvcResult result = mockMvc.perform(post("/api/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "newPassword": "@Werty123"
                        }
                        """)
                .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        // Sprawdź odpowiedź
        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);

        Map<String, Object> responseMap = new ObjectMapper().readValue(responseContent, new TypeReference<>() {});

        String message = responseMap.get("message").toString();
        assertNotNull(message);
        assertEquals("Password updated successfully", message);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    public void testChangePasswordIsSame() throws Exception{
        

        MvcResult result = mockMvc.perform(post("/api/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "newPassword": "@Password123"
                        }
                        """)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Sprawdź odpowiedź
        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);

        Map<String, Object> responseMap = new ObjectMapper().readValue(responseContent, new TypeReference<>() {});

        String message = responseMap.get("message").toString();
        assertNotNull(message);
        assertEquals("New password cannot be the same as the old password", message);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    public void testChangePasswordBadPattern() throws Exception{
        

        MvcResult result = mockMvc.perform(post("/api/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "newPassword": "badPattern"
                        }
                        """)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Sprawdź odpowiedź
        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);

        Map<String, Object> responseMap = new ObjectMapper().readValue(responseContent, new TypeReference<>() {});

        String message = responseMap.get("message").toString();
        assertNotNull(message);
        assertEquals("Password must contain small and big letters, one special sign and numbers", message);
    }
}
