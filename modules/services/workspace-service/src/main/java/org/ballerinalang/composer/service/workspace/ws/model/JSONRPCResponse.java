/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
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

package org.ballerinalang.composer.service.workspace.ws.model;

// Should follow the model: On Success -> {"jsonrpc": "2.0", "result": -19, "id": 2}
// On Error -> {"jsonrpc": "2.0", "error": {"code": -32601, "message": "Method not found"}, "id": "1"}
public class JSONRPCResponse {
    String jsonrpc;
    int id;
    Object result;

    public JSONRPCResponse() {

    }
    public String getJsonrpc() {
        return "2.0";
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public JSONRPCError getError() {
        return error;
    }

    public void setError(JSONRPCError error) {
        this.error = error;
    }

    JSONRPCError error;

    public JSONRPCResponse buildResponseString(JSONRPCRequest jsonrpcRequest, Object responseObj){
        // Set the id of the response same as the id of the request
        this.setId(jsonrpcRequest.getId());
        this.setJsonrpc(this.getJsonrpc());
        // If there's an error in the response
        if (responseObj instanceof JSONRPCError) {
            this.setError((JSONRPCError) responseObj);
        } else {
            this.setResult(responseObj);
        }
        return this;
    }
}
