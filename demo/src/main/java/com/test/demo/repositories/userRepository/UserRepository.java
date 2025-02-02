package com.test.demo.repositories.userRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.test.demo.entities.user.User;
import com.test.demo.entities.user.UserDTO;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    
    Optional<User> findByEmail(String email);
    Optional<User> findByName(String name);

    @Query("SELECT new com.test.demo.entities.user.UserDTO(u.id, u.name, u.email) FROM User u WHERE u.email = :email")
    Optional<UserDTO> findUserDTOByEmail(String email);

    @Query("SELECT new com.test.demo.entities.user.UserDTO(u.id, u.name, u.email) FROM User u WHERE u.id = :id")
    Optional<UserDTO> findUserDTOById(String id);

    @Query("SELECT new com.test.demo.entities.user.UserDTO(u.id, u.name, u.email) FROM User u WHERE u.role = 'USER'")
    List<UserDTO> findUsersbyUserRole();
}
