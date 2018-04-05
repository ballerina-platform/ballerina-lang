// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/http;

int counter = 0;

function testSuccessScenario () returns (http:Response | http:HttpConnectorError) {

    endpoint http:ClientEndpoint backendClientEP {
    lbMode: {
        failoverCodes : [400, 404, 502],
        interval : 0
    },
    targets: [
             {uri: "http://invalidEP"},
             {uri: "http://localhost:8080"}],
    endpointTimeout:5000
    };

    http:Response clientResponse = {};
    http:HttpConnectorError err = {};
    http:Request request = {};
    http:Failover foClient = check <http:Failover>backendClientEP.httpClient;
    MockClient mockClient1 = {serviceUri: "http://invalidEP"};
    MockClient mockClient2 = {serviceUri: "http://localhost:8080", config:{endpointTimeout:1000}};
    http:HttpClient[] httpClients = [mockClient1, mockClient2];
    foClient.failoverInferredConfig.failoverClientsArray = httpClients;

    while (counter < 2) {
       request = {};
       match foClient.get("/hello", request) {
            http:Response res => {
                clientResponse = res;
            }
            http:HttpConnectorError httpConnectorError => {
                err = httpConnectorError;
            }
        }
        counter = counter + 1;
    }
    return clientResponse;
}

function testFailureScenario () returns (http:Response | http:HttpConnectorError) {
    endpoint http:ClientEndpoint backendClientEP {
    lbMode: {
        failoverCodes : [400, 404, 502],
        interval : 0
    },
    targets: [
             {uri: "http://invalidEP"},
             {uri: "http://localhost:50000000"}],
    endpointTimeout:5000
    };
    http:Response clientResponse = {};
    http:HttpConnectorError err = {};
    http:Request request = {};
    http:Failover foClient = check <http:Failover>backendClientEP.httpClient;
    MockClient mockClient1 = {serviceUri: "http://invalidEP"};
    MockClient mockClient2 = {serviceUri: "http://localhost:50000000", config:{endpointTimeout:1000}};
    http:HttpClient[] httpClients = [mockClient1, mockClient2];
    foClient.failoverInferredConfig.failoverClientsArray = httpClients;

    while (counter < 1) {
       request = {};
       match foClient.get("/hello", request) {
            http:Response res => {
                clientResponse = res;
            }
            http:HttpConnectorError httpConnectorError => {
                err = httpConnectorError;
            }
        }
        counter = counter + 1;
    }
    return err;
}

public type MockClient {
    string serviceUri;
    http:ClientEndpointConfiguration config;
}

public function <MockClient client> post (string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
    http:HttpConnectorError httpConnectorError;
    httpConnectorError.message = "Unsupported fuction for MockClient";
    return httpConnectorError;
}

public function <MockClient client> head (string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
    http:HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Unsupported fuction for MockClient";
    return httpConnectorError;
}

public function <MockClient client> put (string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
    http:HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Unsupported fuction for MockClient";
    return httpConnectorError;
}

public function <MockClient client> execute (string httpVerb, string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
    http:HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Unsupported fuction for MockClient";
    return httpConnectorError;
}

public function <MockClient client> patch (string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
    http:HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Unsupported fuction for MockClient";
    return httpConnectorError;
}

public function <MockClient client> delete (string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
    http:HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Unsupported fuction for MockClient";
    return httpConnectorError;
}

public function <MockClient client> get (string path, http:Request req) returns (http:Response | http:HttpConnectorError){
    http:Response response = {};
    http:HttpConnectorError err = {};

    match  handleFailoverScenario(counter) {
        http:Response res => {
            response = res;
        }
        http:HttpConnectorError httpConnectorError => {
            string message = httpConnectorError.message;
            response.statusCode = httpConnectorError.statusCode;
            response.setStringPayload(message);
        }
    }
    return response;
}

public function <MockClient client> options (string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
    http:HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Unsupported fuction for MockClient";
    return httpConnectorError;
}

public function <MockClient client> forward (string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
    http:HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Unsupported fuction for MockClient";
    return httpConnectorError;
}

public function <MockClient client> submit (string httpVerb, string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
    http:HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Unsupported fuction for MockClient";
    return httpConnectorError;
}

public function <MockClient client> getResponse (http:HttpHandle handle)  returns (http:Response | http:HttpConnectorError) {
    http:HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Unsupported fuction for MockClient";
    return httpConnectorError;
}

public function <MockClient client> hasPromise (http:HttpHandle handle) returns (boolean) {
    return false;
}

public function <MockClient client> getNextPromise (http:HttpHandle handle) returns (http:PushPromise | http:HttpConnectorError) {
    http:HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Unsupported fuction for MockClient";
    return httpConnectorError;
}

public function <MockClient client> getPromisedResponse (http:PushPromise promise) returns (http:Response | http:HttpConnectorError) {
    http:HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Unsupported fuction for MockClient";
    return httpConnectorError;
}

public function <MockClient client> rejectPromise (http:PushPromise promise) returns (boolean) {
    return false;
}

function handleFailoverScenario (int count) returns (http:Response | http:HttpConnectorError) {
    http:Response inResponse = {};
    http:HttpConnectorError err = {};
    if (count == 0) {
        err.message = "Connection refused";
        err.statusCode = 502;
        return err;
    } else {
        inResponse.statusCode = 200;
        return inResponse;
    }
}
