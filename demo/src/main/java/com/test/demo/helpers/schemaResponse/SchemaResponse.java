package com.test.demo.helpers.schemaResponse;

import java.util.List;

import com.test.demo.helpers.groupedTable.GroupedTable;
import com.test.demo.helpers.link.Link;

public class SchemaResponse {
    private List<GroupedTable> groupedTables;
    private List<Link> relationships;
    private String message;

    public SchemaResponse() {}

    public SchemaResponse(List<GroupedTable> groupedTables, List<Link> relationships, String message) {
        this.groupedTables = groupedTables;
        this.relationships = relationships;
        this.message = message;
    }

    public List<GroupedTable> getGroupedTables() {
        return groupedTables;
    }

    public void setGroupedTables(List<GroupedTable> groupedTables) {
        this.groupedTables = groupedTables;
    }

    public List<Link> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<Link> relationships) {
        this.relationships = relationships;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
