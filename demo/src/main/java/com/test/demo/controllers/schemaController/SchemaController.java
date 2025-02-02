package com.test.demo.controllers.schemaController;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.test.demo.entities.databasePermission.DatabasePermission;
import com.test.demo.exceptions.databaseException.DatabaseException;
import com.test.demo.helpers.schemaResponse.SchemaResponse;
import com.test.demo.requests.schemaRequest.SchemaRequest;
import com.test.demo.services.databasePermissionService.DatabasePermissionService;
import com.test.demo.services.schemaService.SchemaService;

@RestController
@RequestMapping("/api")
public class SchemaController {

    @Autowired
    private SchemaService schemaService;

    @Autowired
    private DatabasePermissionService databasePermissionService;

    @PostMapping("/database-schema")
    public ResponseEntity<?> generateSchema(@RequestBody SchemaRequest request) {
        try {


            schemaService.validateDatabaseConnection(request);

            // Sprawdzanie uprawnień użytkownika (jeśli nie admin)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails user = (UserDetails) authentication.getPrincipal();
            List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
            
            if (!"ROLE_ADMIN".equalsIgnoreCase( roles.getFirst())) {
                boolean hasPermission = databasePermissionService.checkPermission(request.getUserId(),request.getDatabase(), request.getHost() );
                if (!hasPermission) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "User does not have permission to access this database."));
                }
            }
            SchemaResponse response = schemaService.generateSchema(request);
            Map<String, Object> tablesResponse = new HashMap<>();
            Map<String, Object> formattedResponse = new HashMap<>();
            tablesResponse.put("nodes", response.getGroupedTables().stream()
                .map(table -> {
                    Map<String, Object> node = new HashMap<>();
                    node.put("id", table.getId());
                    node.put("columns", table.getColumns());
                    node.put("x", table.getX());
                    node.put("y", table.getY());
                    return node;
                }).collect(Collectors.toList()));

            tablesResponse.put("links", response.getRelationships().stream()
                .map(link -> {
                    Map<String, Object> relationship = new HashMap<>();
                    relationship.put("source", link.getSource());
                    relationship.put("target", link.getTarget());
                    relationship.put("foreignKeyPosition", link.getForeignKeyPosition());
                    relationship.put("primaryKeyPosition", link.getPrimaryKeyPosition());
                    return relationship;
                }).collect(Collectors.toList()));

            formattedResponse.put("message", response.getMessage());
            formattedResponse.put("tables", tablesResponse);

            return ResponseEntity.ok(formattedResponse);
        } catch (DatabaseException ex) {
            return ResponseEntity.status(ex.getHttpStatus()).body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", ex.getMessage()));
        }
    }


}
