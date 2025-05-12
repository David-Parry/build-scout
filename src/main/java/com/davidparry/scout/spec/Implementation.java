package com.davidparry.scout.spec;



/**
 * Represents server implementation details.
 */
public class Implementation {
    private String name;
    
    private String version;
    
    // Default constructor
    public Implementation() {
    }
    
    // Constructor with all fields
    public Implementation(String name, String version) {
        this.name = name;
        this.version = version;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    @Override
    public String toString() {
        return "Implementation[" +
                "name=" + name +
                ", version=" + version +
                "]";
    }
}