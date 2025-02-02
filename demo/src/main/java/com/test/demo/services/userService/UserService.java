package com.test.demo.services.userService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.test.demo.entities.user.User;
import com.test.demo.entities.user.UserDTO;
import com.test.demo.repositories.userRepository.UserRepository;
import com.test.demo.requests.userRegistrationRequest.UserRegistrationRequest;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(UserRegistrationRequest request) throws Exception {
        // Sprawdzenie unikalności użytkownika
        if (userRepository.findByEmail(request.getEmail()).isPresent() ||
            userRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Username and email must be unique");
        }

       // throw new Exception("test");

        //Hashowanie hasła
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Tworzenie nowego użytkownika
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(hashedPassword);
        user.setRole("USER");

        userRepository.save(user);
    }

    public List<UserDTO> getUsers() throws RuntimeException {
        try{
            List<UserDTO> users = userRepository.findUsersbyUserRole();
            return users;
        } catch(RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public UserDTO getUser(String id) throws RuntimeException {
        Optional<UserDTO> userOptional = userRepository.findUserDTOById(id);
        if(!userOptional.isPresent()){
            throw new RuntimeException("User not found");
        }
        UserDTO user = userOptional.get();
        return user;
    }

    public UserDTO getUserByEmail(String email) throws RuntimeException {
        Optional<UserDTO> userOptional = userRepository.findUserDTOByEmail(email);
        if(!userOptional.isPresent()){
            throw new RuntimeException("User not found");
        }
        UserDTO user = userOptional.get();
        return user;
    }

    public void deleteUserById(String id) throws RuntimeException {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public void updatePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User does not exist"));

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("New password cannot be the same as the old password");
        }

        // Walidacja hasła
        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$";
        if (!newPassword.matches(passwordPattern)) {
            throw new IllegalArgumentException("Password must contain small and big letters, one special sign and numbers");
        }

        // Hashowanie hasła
        String hashedPassword = passwordEncoder.encode(newPassword);

        // Aktualizacja hasła w bazie
        user.setPassword(hashedPassword);
        userRepository.save(user);
    }
}

