package com.test.demo.services.schemaService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.test.demo.exceptions.databaseException.DatabaseException;
import com.test.demo.helpers.foreignKey.ForeignKey;
import com.test.demo.helpers.groupedTable.GroupedTable;
import com.test.demo.helpers.link.Link;
import com.test.demo.helpers.schemaResponse.SchemaResponse;
import com.test.demo.requests.permissionRequestDTO.PermissionRequestDTO;
import com.test.demo.requests.schemaRequest.SchemaRequest;

import org.postgresql.ds.PGSimpleDataSource;

@Service
public class SchemaService {

    public SchemaResponse generateSchema(SchemaRequest request) throws Exception {
        // Konfiguracja połączenia do bazy danych
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUser(request.getUser());
        dataSource.setPassword(request.getPassword());
        dataSource.setDatabaseName(request.getDatabase());
        dataSource.setServerNames(new String[]{request.getHost()});
        dataSource.setPortNumbers(new int[]{request.getPort()});

        try (Connection connection = dataSource.getConnection()) {
            // Zapytanie o tabele i kolumny
            Map<String, List<String>> tables = fetchTables(connection);

            // Klucze główne
            Map<String, String> primaryKeys = fetchPrimaryKeys(connection);

            // Klucze obce
            List<ForeignKey> foreignKeys = fetchForeignKeys(connection);

            // Relacje
            List<Link> links = classifyRelationships(foreignKeys, primaryKeys, tables);

            // Grupowanie tabel
            List<GroupedTable> groupedTables = groupTablesBFS(tables, links);

            return new SchemaResponse(groupedTables, links, "Schema generating Successful");

        } catch (SQLException ex) {
            throw new DatabaseException("Database connection error: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);    
        }
    }

    public void validateDatabaseConnection(SchemaRequest request) {
        String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", request.getHost(), request.getPort(), request.getDatabase());

        try (Connection connection = DriverManager.getConnection(jdbcUrl, request.getUser(), request.getPassword())) {
            // Wykonaj prosty test połączenia
            try (Statement statement = connection.createStatement()) {
                statement.executeQuery("SELECT 1");
            }
        } catch (SQLException e) {
            switch (e.getSQLState()) {
                case "3D000":
                    throw new DatabaseException("Database not found. Check your database name and try again.", HttpStatus.NOT_FOUND);
                case "28P01":
                    throw new DatabaseException("Database user authorization failed. Check your user data and try again.", HttpStatus.FORBIDDEN);
                case "08001":
                    throw new DatabaseException("Host not found. Check your host name and try again.", HttpStatus.NOT_FOUND);
                case "08006":
                    throw new DatabaseException("Connection refused. Check your port number and try again.", HttpStatus.FORBIDDEN);
                default:
                    throw new DatabaseException("Database connection error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }
    }

    private Map<String, List<String>> fetchTables(Connection connection) throws SQLException {
        String query = """
            SELECT table_name, column_name
            FROM information_schema.columns
            WHERE table_schema = 'public'
        """;

        Map<String, List<String>> tables = new HashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String table = rs.getString("table_name");
                String column = rs.getString("column_name");
                tables.computeIfAbsent(table, k -> new ArrayList<>()).add(column);
            }
        }
        return tables;
    }

    private Map<String, String> fetchPrimaryKeys(Connection connection) throws SQLException {
        String query = """
            SELECT kcu.table_name, kcu.column_name
            FROM information_schema.key_column_usage kcu
            JOIN information_schema.table_constraints tc
            ON kcu.constraint_name = tc.constraint_name
            WHERE tc.constraint_type = 'PRIMARY KEY';
        """;
    
        Map<String, String> primaryKeys = new HashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                primaryKeys.put(rs.getString("table_name"), rs.getString("column_name"));
            }
        }
        return primaryKeys;
    }

    private List<ForeignKey> fetchForeignKeys(Connection connection) throws SQLException {
        String query = """
            SELECT tc.table_name AS source_table,
                   kcu.column_name AS source_column,
                   ccu.table_name AS target_table,
                   ccu.column_name AS target_column
            FROM information_schema.table_constraints tc
            JOIN information_schema.key_column_usage kcu
            ON tc.constraint_name = kcu.constraint_name
            JOIN information_schema.constraint_column_usage ccu
            ON ccu.constraint_name = tc.constraint_name
            WHERE tc.constraint_type = 'FOREIGN KEY';
        """;
    
        List<ForeignKey> foreignKeys = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                foreignKeys.add(new ForeignKey(
                    rs.getString("source_table"),
                    rs.getString("source_column"),
                    rs.getString("target_table"),
                    rs.getString("target_column")
                ));
            }
        }
        return foreignKeys;
    }

    private List<Link> classifyRelationships(List<ForeignKey> foreignKeys, Map<String, String> primaryKeys, Map<String, List<String>> tables) {
        List<Link> links = new ArrayList<>();
    
        for (ForeignKey fk : foreignKeys) {
            Integer foreignKeyPosition = tables.get(fk.getSourceTable()).indexOf(fk.getSourceColumn());
            Integer primaryKeyPosition = tables.get(fk.getTargetTable()).indexOf(fk.getTargetColumn());
    
            String type = primaryKeys.containsKey(fk.getSourceTable()) ? "1:1" : "1:N";
            links.add(new Link(fk.getSourceTable(), fk.getTargetTable(), foreignKeyPosition, primaryKeyPosition, type));
        }
    
        return links;
    }

    private List<GroupedTable> groupTablesBFS(Map<String, List<String>> tables, List<Link> links) {
        Map<String, List<String>> relationMap = new HashMap<>();
        tables.keySet().forEach(table -> relationMap.put(table, new ArrayList<>()));

        for (Link link : links) {
            relationMap.get(link.getSource()).add(link.getTarget());
            relationMap.get(link.getTarget()).add(link.getSource());
        }

        Set<String> visited = new HashSet<>();
        List<GroupedTable> groupedTables = new ArrayList<>();

        int x = 100;
        int y = 100;
        int groupNum = 1;

        for (String table : tables.keySet()) {
            if (!visited.contains(table)) {
                List<String> group = bfs(table, relationMap, visited);
                for (String groupedTable : group) {
                    groupedTables.add(new GroupedTable(groupedTable, tables.get(groupedTable),
                    x,y,groupNum));
                    x += 200;
                    y += 100;
                }
            }
            x=100;
        }
        return groupedTables;
    }

    private List<String> bfs(String start, Map<String, List<String>> relationMap, Set<String> visited) {
        List<String> group = new ArrayList<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            String table = queue.poll();
            if (!visited.contains(table)) {
                visited.add(table);
                group.add(table);
                queue.addAll(relationMap.get(table));
            }
        }
        return group;
    }
}
