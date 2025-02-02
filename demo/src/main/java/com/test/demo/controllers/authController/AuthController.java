package com.test.demo.controllers.authController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.test.demo.requests.loginRequest.LoginRequest;
import com.test.demo.services.userService.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @GetMapping("/session-info")
    public ResponseEntity<Map<String, Object>> getSessionInfo(HttpServletRequest request, HttpSession session) {
        Map<String, Object> sessionInfo = new HashMap<>();
        try {
            String sessionId = "no session";
            if(session != null) {
                sessionId = session.getId();
            } 
            System.out.println("Session ID: " + (session != null ? session.getId() : "No session"));
            // Pobieramy dane zalogowanego użytkownika
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if(authentication == null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Session expired"));
            }
            UserDetails user = (UserDetails) authentication.getPrincipal();
            List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

            String id = userService.getUserByEmail(user.getUsername()).getId();
        
            sessionInfo.put("user", user.getUsername());
            sessionInfo.put("id", id);
            sessionInfo.put("roles", roles.getFirst());
            sessionInfo.put("sessionId", sessionId);

            return ResponseEntity.ok(sessionInfo);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/csrf-token")
    // public ResponseEntity<Map<String, String>> getCsrfToken(HttpServletRequest request) {
    //     CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    //     return ResponseEntity.ok(Map.of("token", csrfToken.getToken()));
    // }
    public ResponseEntity<Void> getCsrfToken() {
        // Token CSRF jest automatycznie generowany przez Spring Security
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request,HttpSession session) {
        try {
            // Próba uwierzytelnienia
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(), 
                loginRequest.getPassword()
            );

            Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            //HttpSession session = request.getSession(true);
            if (session != null) {
                session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
                System.out.println("Session created with ID: " + session.getId() + "\n");
            }

            return ResponseEntity.ok("Authenticated");
        } catch (BadCredentialsException e) {
            // W przypadku niepoprawnych danych logowania zwróć komunikat o błędzie
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body("Invalid username or password");
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // Wylogowanie użytkownika
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.clearContext();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        return ResponseEntity.ok("Logged out successfully");
        }
    
}
