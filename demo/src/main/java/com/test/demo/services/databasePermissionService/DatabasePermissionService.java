package com.test.demo.services.databasePermissionService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.test.demo.entities.databasePermission.DatabasePermission;
import com.test.demo.entities.databasePermission.DatabasePermissionDTO;
import com.test.demo.entities.user.User;
import com.test.demo.exceptions.databaseException.DatabaseException;
import com.test.demo.repositories.databasePermissionRepository.DatabasePermissionRepository;
import com.test.demo.repositories.userRepository.UserRepository;
import com.test.demo.requests.permissionRequestDTO.PermissionRequestDTO;

import jakarta.transaction.Transactional;

@Service
public class DatabasePermissionService {

    private final DatabasePermissionRepository databasePermissionRepository;
    private final UserRepository userRepository;

    public DatabasePermissionService(DatabasePermissionRepository databasePermissionRepository, UserRepository userRepository) {
        this.databasePermissionRepository = databasePermissionRepository;
        this.userRepository = userRepository;
    }

    public List<DatabasePermissionDTO> getPermissionsForUser(String userId) throws Exception {
        List<DatabasePermissionDTO> permissions = databasePermissionRepository.findDatabasePermissionsByUserId(userId);
        return permissions;
    }

    @Transactional
    public void deletePermission(String userId,String host,String name) throws Exception{
        if(!databasePermissionRepository.findUniqueDatabasePermission(userId, name, host).isPresent()){
            throw new Exception("That permission does not exists; deleting failed");
        } else{
            databasePermissionRepository.deleteDatabasePermission(userId,name,host);
        }
    }

    public boolean checkPermission(String userId, String name, String host) {
        return (!databasePermissionRepository.findUniqueDatabasePermission(userId, name, host).isPresent()) ? false:true;
    }

    public void validateDatabaseConnection(PermissionRequestDTO dto) {
        String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", dto.getHost(), dto.getPort(), dto.getDatabase());

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dto.getUser(), dto.getPassword())) {
            // Wykonaj prosty test połączenia
            try (Statement statement = connection.createStatement()) {
                statement.executeQuery("SELECT 1");
            }
        } catch (SQLException e) {
            switch (e.getSQLState()) {
                case "3D000":
                    throw new DatabaseException("Database not found. Check your database name and try again.", HttpStatus.NOT_FOUND);
                case "28P01":
                    throw new DatabaseException("Database user authorization failed. Check your user data and try again.", HttpStatus.FORBIDDEN);
                case "08001":
                    throw new DatabaseException("Host not found. Check your host name and try again.", HttpStatus.NOT_FOUND);
                case "08006":
                    throw new DatabaseException("Connection refused. Check your port number and try again.", HttpStatus.FORBIDDEN);
                default:
                    throw new DatabaseException("Database connection error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }
    }

    public void addPermission(PermissionRequestDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getUserId()));
        DatabasePermission permission = new DatabasePermission();
        permission.setUser(user);
        permission.setDbName(dto.getDatabase());
        permission.setDbHost(dto.getHost());
        permission.setDbPort(dto.getPort());
        permission.setDbUser(dto.getUser());

        databasePermissionRepository.save(permission);
    }

    private void handleDatabaseError(SQLException e) {
        switch (e.getSQLState()) {
            case "3D000":
                throw new DatabaseException("Database not found. Check your database name and try again.", HttpStatus.NOT_FOUND);
            case "28P01":
                throw new DatabaseException("Database user authorization failed. Check your user data and try again.", HttpStatus.FORBIDDEN);
            case "08001":
                throw new DatabaseException("Host not found. Check your host name and try again.", HttpStatus.NOT_FOUND);
            case "08006":
                throw new DatabaseException("Connection refused. Check your port number and try again.", HttpStatus.FORBIDDEN);
            default:
                throw new DatabaseException("Database connection error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}