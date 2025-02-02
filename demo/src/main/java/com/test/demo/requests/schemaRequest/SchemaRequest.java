package com.test.demo.requests.schemaRequest;

public class SchemaRequest {
    private String database;
    private String host;
    private String user;
    private String password;
    private int port;
    private String role;
    private String userId;

    public SchemaRequest(String database, String host, String user, String password, int port, String role, String userId) {
        this.database = database;
        this.host = host;
        this.user = user;
        this.password = password;
        this.port = port;
        this.role = role;
        this.userId = userId;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
