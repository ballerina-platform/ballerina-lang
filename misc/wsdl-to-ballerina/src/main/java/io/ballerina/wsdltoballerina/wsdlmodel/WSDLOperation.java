/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.wsdltoballerina.wsdlmodel;

public class WSDLOperation {
    private String operationName;
    private String operationAction;
    private WSDLPayload operationInput;
    private WSDLPayload operationOutput;

    public WSDLOperation(String operationName, String operationAction) {
        this.operationName = operationName;
        this.operationAction = operationAction;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public String getOperationAction() {
        return operationAction;
    }

    public void setOperationAction(String operationAction) {
        this.operationAction = operationAction;
    }

    public WSDLPayload getOperationInput() {
        return operationInput;
    }

    public void setOperationInput(WSDLPayload operationInput) {
        this.operationInput = operationInput;
    }

    public WSDLPayload getOperationOutput() {
        return operationOutput;
    }

    public void setOperationOutput(WSDLPayload operationOutput) {
        this.operationOutput = operationOutput;
    }
}
