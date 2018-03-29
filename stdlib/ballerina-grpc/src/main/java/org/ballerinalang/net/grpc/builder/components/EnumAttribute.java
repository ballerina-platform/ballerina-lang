package org.ballerinalang.net.grpc.builder.components;

// TODO: 3/28/18 need to support more complex types
public class EnumAttribute {
    private String name;
    private String eoe;
    
    public EnumAttribute(String name, String eoe) {
        this.name = name;
        this.eoe = eoe;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEoe() {
        return eoe;
    }
    
    public void setEoe(String eoe) {
        this.eoe = eoe;
    }
}
