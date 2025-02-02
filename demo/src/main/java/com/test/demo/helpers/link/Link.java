package com.test.demo.helpers.link;

public class Link {
    private String source; 
    private String target; 
    private int foreignKeyPosition; 
    private int primaryKeyPosition; 
    private String type; 

    public Link(String source, String target, int foreignKeyPosition, int primaryKeyPosition, String type) {
        this.source = source;
        this.target = target;
        this.foreignKeyPosition = foreignKeyPosition;
        this.primaryKeyPosition = primaryKeyPosition;
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public int getForeignKeyPosition() {
        return foreignKeyPosition;
    }

    public void setForeignKeyPosition(int foreignKeyPosition) {
        this.foreignKeyPosition = foreignKeyPosition;
    }

    public int getPrimaryKeyPosition() {
        return primaryKeyPosition;
    }

    public void setPrimaryKeyPosition(int primaryKeyPosition) {
        this.primaryKeyPosition = primaryKeyPosition;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
