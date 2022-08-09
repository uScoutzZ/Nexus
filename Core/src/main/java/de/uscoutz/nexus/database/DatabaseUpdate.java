package de.uscoutz.nexus.database;

public class DatabaseUpdate {

    private String key;
    private Object value;

    public DatabaseUpdate(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }
}
