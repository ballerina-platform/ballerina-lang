package org.ballerinalang.net.grpc.builder.components;

import java.util.ArrayList;
import java.util.List;

public class Client {
    private String connectorId;
    private List<StubObject> stubObjects = new ArrayList<>();
    
    public Client(String connectorId) {
        this.connectorId = connectorId;
    }
    
    public  void addStubObjects(StubObject stubObject){
        stubObjects.add(stubObject);
    }
    
    public String getConnectorId() {
        return connectorId;
    }
    
    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }
    
    public List<StubObject> getStubObjects() {
        return stubObjects;
    }
    
    public void setStubObjects(List<StubObject> stubObjects) {
        this.stubObjects = stubObjects;
    }
}
