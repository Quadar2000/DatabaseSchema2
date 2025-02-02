package com.test.demo.repositories.accessTokenRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.test.demo.entities.accessToken.AccessToken;

import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken, String> {
    Optional<AccessToken> findByTokenAndExpiryDateAfter(String token, LocalDateTime currentDate);

    @Modifying
    @Transactional
    @Query("DELETE FROM AccessToken a WHERE a.expiryDate < :currentDate")
    void deleteExpiredTokens(@Param("currentDate") LocalDateTime currentDate);

    @Modifying
    @Transactional
    @Query("DELETE FROM AccessToken a WHERE a.user.id = :userId")
    void deleteTokensByUserId(@Param("userId") String userId);
}
