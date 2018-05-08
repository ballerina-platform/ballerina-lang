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

import static org.ballerinalang.net.grpc.GrpcConstants.BRACKET_CLOSE;
import static org.ballerinalang.net.grpc.GrpcConstants.BRACKET_OPEN;
import static org.ballerinalang.net.grpc.GrpcConstants.COMMA;
import static org.ballerinalang.net.grpc.GrpcConstants.DIAMOND_CAST_CLOSE;
import static org.ballerinalang.net.grpc.GrpcConstants.DIAMOND_CAST_OPEN;
import static org.ballerinalang.net.grpc.GrpcConstants.IGNORE_CAST;
import static org.ballerinalang.net.grpc.GrpcConstants.INIT_EMPTY_STUB;
import static org.ballerinalang.net.grpc.GrpcConstants.INPUT_TYPE_NAME;
import static org.ballerinalang.net.grpc.GrpcConstants.RESULT_TYPE_NAME;
import static org.ballerinalang.net.grpc.GrpcConstants.SPACE;

/**
 * Bean class of blocking function object.
 */
public class BlockingFunction {
    private String stubTypeName;
    private String connectorId;
    private String operationId;
    private String inputDataType;
    private String outputDataType;
    private String initEmptyStub;
    private String methodId;
    private String inputComma = COMMA;
    private String inputAttributeName = INPUT_TYPE_NAME;
    private String outputComma = COMMA;
    private String resultCast = RESULT_TYPE_NAME;
    private String resultOut = RESULT_TYPE_NAME;
    private String castSymbolOpen = DIAMOND_CAST_OPEN;
    private String castSymbolClose = DIAMOND_CAST_CLOSE;
    private String openBracket = BRACKET_OPEN;
    private String closeBracket = BRACKET_CLOSE;
    private String space = SPACE;
    
    public BlockingFunction(String stubTypeName, String connectorId, String operationId, String inputDataType,
                            String outputDataType, String methodId) {
        this.stubTypeName = stubTypeName;
        this.connectorId = connectorId;
        this.operationId = operationId;
        this.inputDataType = inputDataType;
        this.outputDataType = outputDataType;
        this.methodId = methodId;
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
    
    public String getOutputDataType() {
        return outputDataType;
    }
    
    public void setOutputDataType(String outputDataType) {
        this.outputDataType = outputDataType;
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
    
    public String getOutputComma() {
        return outputComma;
    }
    
    public void setOutputComma(String outputComma) {
        this.outputComma = outputComma;
    }
    
    public void initEmptyStruct() {
        this.initEmptyStub = INIT_EMPTY_STUB;
    }
    
    public String getInitEmptyStub() {
        return initEmptyStub;
    }
    
    public void ignoreCast() {
        this.castSymbolClose = null;
        this.castSymbolOpen = null;
        this.resultOut = null;
        this.openBracket = null;
        this.closeBracket = null;
        this.resultCast = IGNORE_CAST;
    }
    
    public String getResultCast() {
        return resultCast;
    }
    
    public String getResultOut() {
        return resultOut;
    }
    
    public String getCastSymbolOpen() {
        return castSymbolOpen;
    }
    
    public String getCastSymbolClose() {
        return castSymbolClose;
    }
    
    public String getOpenBracket() {
        return openBracket;
    }
    
    public String getCloseBracket() {
        return closeBracket;
    }
    
    public String getSpace() {
        return space;
    }
}
