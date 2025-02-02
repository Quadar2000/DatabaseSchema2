package com.test.demo.controllers.accessTokenController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.test.demo.services.accessTokenService.AccessTokenService;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AccessTokenController {

    @Autowired
    private AccessTokenService accessTokenService;

    // Endpoint do generowania tokenu
    @PostMapping("/generate-token")
    public ResponseEntity<?> generateToken(@RequestBody Map<String, String> request) {
        try{
            String userId = request.get("userId");
            String token = accessTokenService.generateToken(userId);
            return ResponseEntity.ok(Map.of("token", token));
        
        } catch (RuntimeException e) {
            // Obsługa wyjątku rzuconego w `generateToken` w serwisie
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // Endpoint do walidacji tokenu
    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> request) {
        try{
            String token = request.get("token");

            boolean isValid = accessTokenService.validateToken(token);

            if (isValid) {
                return ResponseEntity.ok(Map.of("success", true));
            } else {
                return ResponseEntity.status(400).body(Map.of("message", "Invalid or expired token"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("message",e.getMessage()));
        }    
    }
}