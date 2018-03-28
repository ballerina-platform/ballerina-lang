package org.ballerinalang.net.grpc.builder.components;

// TODO: 3/28/18 need to support more complex types
public class EnumAttribute {
    private String name;
    
    public EnumAttribute(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
