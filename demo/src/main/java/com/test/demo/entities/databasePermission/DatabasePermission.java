package com.test.demo.entities.databasePermission;

import com.test.demo.entities.user.User;

import jakarta.persistence.*;

@Entity
public class DatabasePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", referencedColumnName = "id",nullable = false)
    private User user;

    @Column(nullable = false)
    private String dbName;

    @Column(nullable = false)
    private String dbUser;

    @Column(nullable = false)
    private String dbHost;

    @Column(nullable = false)
    private String dbPort;

    // Gettery i Settery
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDbName() { return dbName; }
    public void setDbName(String dbName) { this.dbName = dbName; }

    public String getDbUser() { return dbUser; }
    public void setDbUser(String dbUser) { this.dbUser = dbUser; }

    public String getDbHost() { return dbHost; }
    public void setDbHost(String dbHost) { this.dbHost = dbHost; }

    public String getDbPort() { return dbPort; }
    public void setDbPort(String dbPort) { this.dbPort = dbPort; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
