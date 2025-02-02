package com.test.demo.helpers.foreignKey;

public class ForeignKey {
    private String sourceTable; // Tabela źródłowa
    private String sourceColumn; // Kolumna w tabeli źródłowej
    private String targetTable; // Tabela docelowa
    private String targetColumn; // Kolumna w tabeli docelowej

    public ForeignKey(String sourceTable, String sourceColumn, String targetTable, String targetColumn) {
        this.sourceTable = sourceTable;
        this.sourceColumn = sourceColumn;
        this.targetTable = targetTable;
        this.targetColumn = targetColumn;
    }

    public String getSourceTable() {
        return sourceTable;
    }

    public void setSourceTable(String sourceTable) {
        this.sourceTable = sourceTable;
    }

    public String getSourceColumn() {
        return sourceColumn;
    }

    public void setSourceColumn(String sourceColumn) {
        this.sourceColumn = sourceColumn;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
    }

    public String getTargetColumn() {
        return targetColumn;
    }

    public void setTargetColumn(String targetColumn) {
        this.targetColumn = targetColumn;
    }
}
