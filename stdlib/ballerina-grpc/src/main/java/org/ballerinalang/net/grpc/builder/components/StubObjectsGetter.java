package org.ballerinalang.net.grpc.builder.components;

public class StubObjectsGetter {
    private  String connectorId;
    private  String stubTypeName;
    
    public StubObjectsGetter(String connectorId, String stubTypeName) {
        this.connectorId = connectorId;
        this.stubTypeName = stubTypeName;
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
}
