/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.net.grpc.builder.components;

import java.util.ArrayList;
import java.util.List;

/**
 * Bean class of client object.
 */
public class ClientObject {
    private String stubType;
    private String connectorId;
    private List<Stub> stubObjects = new ArrayList<>();
    
    public ClientObject(String connectorId) {
        this.connectorId = connectorId;
    }
    
    public void addStubObjects(Stub stubObject) {
        stubObjects.add(stubObject);
    }
    
    public String getConnectorId() {
        return connectorId;
    }
    
    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }
    
    public List<Stub> getStubObjects() {
        return stubObjects;
    }
    
    public void setStubObjects(List<Stub> stubObjects) {
        this.stubObjects = stubObjects;
    }
    
    public String getStubType() {
        return stubType;
    }
    
    public void setStubType(String stubType) {
        this.stubType = stubType;
    }
}
