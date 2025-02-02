package com.test.demo.controllers.permissionController;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.test.demo.entities.databasePermission.DatabasePermissionDTO;
import com.test.demo.entities.user.UserDTO;
import com.test.demo.exceptions.databaseException.DatabaseException;
import com.test.demo.requests.permissionRequestDTO.PermissionRequestDTO;
import com.test.demo.services.databasePermissionService.DatabasePermissionService;
import com.test.demo.services.userService.UserService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api")
public class PermissionController {

    @Autowired
    private DatabasePermissionService databasePermissionService;

    @Autowired 
    private UserService userService;

    @GetMapping("/get-permissions")
    public ResponseEntity<?> getPermissions(@RequestParam String userId) {
        try {
            List <DatabasePermissionDTO> permissions = databasePermissionService.getPermissionsForUser(userId);
            UserDTO user = userService.getUser(userId);
            return ResponseEntity.ok().body(Map.of("permissions", permissions,"user", user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                     .body(Map.of("message", e.getMessage()));
        } 
    }

    @DeleteMapping("/delete-permission")
    public ResponseEntity<?> deletePermission(@RequestParam String userId,String host,String name) {
        try {
            System.out.println("name:" + name);
            System.out.println("userId:" + userId);
            System.out.println("host:" + host);
            databasePermissionService.deletePermission(userId, host, name);
            return ResponseEntity.ok().body(Map.of("message", "Permission deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                     .body(Map.of("message", e.getMessage()));
        } 
    }
    
    @PostMapping("/grant-permission")
    public ResponseEntity<?> grantPermission(@RequestBody PermissionRequestDTO dto) {
        try {
            //Sprawdzenie, czy użytkownik ma już uprawnienia
            boolean permissionExists = databasePermissionService.checkPermission(dto.getUserId(), dto.getDatabase(), dto.getHost());
            if (permissionExists) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "This user already has permission for that database."));
            }

            // Sprawdzenie połączenia z bazą danych
            databasePermissionService.validateDatabaseConnection(dto);

            // Zapis nowego uprawnienia w bazie
            databasePermissionService.addPermission(dto);

            return ResponseEntity.ok(Map.of("message", "Permission granted successfully"));
        } catch (DatabaseException e) {
            return ResponseEntity.status(e.getHttpStatus())
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
