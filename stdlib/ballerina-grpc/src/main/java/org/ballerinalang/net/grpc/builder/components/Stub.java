package org.ballerinalang.net.grpc.builder.components;

public class Stub {
    private String connectorId;
    private String stubTypeName;
    private String stubType;
    
    public Stub(String connectorId, String stubTypeName, String stubType) {
        this.connectorId = connectorId;
        this.stubTypeName = stubTypeName;
        this.stubType = stubType;
    }
    
    public String getConnectorId() {
        return connectorId;
    }
    
    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }
    
    public String getStubTypeName() {
        return stubTypeName;
    }
    
    public void setStubTypeName(String stubTypeName) {
        this.stubTypeName = stubTypeName;
    }
    
    public String getStubType() {
        return stubType;
    }
    
    public void setStubType(String stubType) {
        this.stubType = stubType;
    }
}
