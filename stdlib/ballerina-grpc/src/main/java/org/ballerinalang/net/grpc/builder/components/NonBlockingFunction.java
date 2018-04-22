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

import static org.ballerinalang.net.grpc.GrpcConstants.COMMA;
import static org.ballerinalang.net.grpc.GrpcConstants.INPUT_TYPE_NAME;
import static org.ballerinalang.net.grpc.GrpcConstants.SPACE;

/**
 * Bean class of non blocking function object.
 */
public class NonBlockingFunction {
    private String stubTypeName;
    private String connectorId;
    private String operationId;
    private String inputDataType;
    private String methodId;
    private String inputComma = COMMA;
    private String inputAttributeName = INPUT_TYPE_NAME;
    private String space = SPACE;
    
    public NonBlockingFunction(String stubTypeName, String connectorId,
                               String operationId, String inputDataType, String methodId) {
        this.operationId = operationId;
        this.inputDataType = inputDataType;
        this.methodId = methodId;
        this.stubTypeName = stubTypeName;
        this.connectorId = connectorId;
    }
    
    public NonBlockingFunction() {
    }
    
    public String getOperationId() {
        return operationId;
    }
    
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }
    
    public String getInputDataType() {
        return inputDataType;
    }
    
    public void setInputDataType(String inputDataType) {
        this.inputDataType = inputDataType;
    }
    
    public String getMethodId() {
        return methodId;
    }
    
    public void setMethodId(String methodId) {
        this.methodId = methodId;
    }
    
    public String getStubTypeName() {
        return stubTypeName;
    }
    
    public void setStubTypeName(String stubTypeName) {
        this.stubTypeName = stubTypeName;
    }
    
    public String getConnectorId() {
        return connectorId;
    }
    
    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }
    
    public String getInputComma() {
        return inputComma;
    }
    
    public void setInputComma(String inputComma) {
        this.inputComma = inputComma;
        if (inputComma == null) {
            this.space = null;
        }
    }
    
    public String getInputAttributeName() {
        return inputAttributeName;
    }
    
    public void setInputAttributeName(String inputAttributeName) {
        this.inputAttributeName = inputAttributeName;
    }
    
    public String getSpace() {
        return space;
    }
}
