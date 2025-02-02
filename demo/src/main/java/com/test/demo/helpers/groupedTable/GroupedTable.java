package com.test.demo.helpers.groupedTable;

import java.util.List;

public class GroupedTable {
    private String id; // Nazwa tabeli
    private List<String> columns; // Lista kolumn
    private int x; // Pozycja X na diagramie
    private int y; // Pozycja Y na diagramie
    private int group; // Grupa tabel

    public GroupedTable(String id, List<String> columns, int x, int y, int group) {
        this.id = id;
        this.columns = columns;
        this.x = x;
        this.y = y;
        this.group = group;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }
}
