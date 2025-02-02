package com.test.demo.entities.user;

import jakarta.persistence.*;
import java.util.Set;

import com.test.demo.entities.accessToken.AccessToken;
import com.test.demo.entities.databasePermission.DatabasePermission;

@Entity
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<DatabasePermission> databasePermissions;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<AccessToken> accessTokens;

    // Gettery i Settery
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Set<DatabasePermission> getDatabasePermissions() { return databasePermissions; }
    public void setDatabasePermissions(Set<DatabasePermission> databasePermissions) {
        this.databasePermissions = databasePermissions;
    }

    public Set<AccessToken> getAccessTokens() { return accessTokens; }
    public void setAccessTokens(Set<AccessToken> accessTokens) {
        this.accessTokens = accessTokens;
    }
}
