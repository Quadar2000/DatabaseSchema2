package com.test.demo.entities.databasePermission;

public class DatabasePermissionDTO {

    private String dbHost;
    private String dbName;
    private String dbPort;
    private String dbUser;

    public DatabasePermissionDTO(String dbHost, String dbName, String dbPort, String dbUser) {
        this.dbHost = dbHost;
        this.dbName = dbName;
        this.dbPort = dbPort;
        this.dbUser = dbUser;
    }

    // Gettery
    public String getDbHost() { return dbHost; }
    public String getDbName() { return dbName; }
    public String getDbPort() { return dbPort; }
    public String getDbUser() { return dbUser; }
}
