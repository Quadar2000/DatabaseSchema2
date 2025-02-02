package com.test.demo.services.accessTokenService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.test.demo.entities.accessToken.AccessToken;
import com.test.demo.entities.user.User;
import com.test.demo.repositories.accessTokenRepository.AccessTokenRepository;
import com.test.demo.repositories.userRepository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccessTokenService {

    @Autowired
    private AccessTokenRepository accessTokenRepository;

    @Autowired
    private UserRepository userRepository;

    public String generateToken(String userId) {
        LocalDateTime now = LocalDateTime.now();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generujemy unikalny token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(5); // Token ważny przez 5 minut

        // Tworzymy nowy obiekt AccessToken
        AccessToken accessToken = new AccessToken();
        accessToken.setUser(user);  // Ustawiamy użytkownika
        accessToken.setToken(token);
        accessToken.setExpiryDate(expiryDate);

        // Zapisujemy token w bazie danych
        accessTokenRepository.save(accessToken);

        accessTokenRepository.deleteExpiredTokens(now);

        return token;
    }

    public boolean validateToken(String token) {
        LocalDateTime now = LocalDateTime.now();
        Optional<AccessToken> dbToken = accessTokenRepository.findByTokenAndExpiryDateAfter(token, now);

        if (dbToken.isPresent()) {
            //accessTokenRepository.delete(dbToken.get());
            return true;
        }
        return false;
    }
}
