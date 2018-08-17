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

/**
 * Bean class of client endpoint object.
 */
public class EndPoint {
    private String connectorId;
    private String connectorIdName;
    
    public EndPoint(String connectorId) {
        this.connectorId = connectorId;
        this.connectorIdName = Character.toLowerCase(connectorId.charAt(0)) + connectorId.substring(1);
    }
    
    public String getConnectorId() {
        return connectorId;
    }
    
    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }
    
    public String getConnectorIdName() {
        return connectorIdName;
    }
    
    public void setConnectorIdName(String connectorIdName) {
        this.connectorIdName = connectorIdName;
    }
}
