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

    endpoint http:Client backendClientEP {
    lbMode: {
        failoverCodes : [400, 404, 502],
        interval : 0
    },
    targets: [
             {url: "http://invalidEP"},
             {url: "http://localhost:8080"}],
    timeoutMillis:5000
    };

    http:Response clientResponse = new;
    http:Failover foClient = check <http:Failover>backendClientEP.getClient();
    MockClient mockClient1 = new;
    MockClient mockClient2 = new;
    http:HttpClient[] httpClients = [<http:HttpClient> mockClient1, <http:HttpClient> mockClient2];
    foClient.failoverInferredConfig.failoverClientsArray = httpClients;

    while (counter < 2) {
       http:Request request = new;
       match foClient.get("/hello", request) {
            http:Response res => {
                clientResponse = res;
            }
            http:HttpConnectorError httpConnectorError => {
            }
        }
        counter = counter + 1;
    }
    return clientResponse;
}

function testFailureScenario () returns (http:Response | http:HttpConnectorError) {
    endpoint http:Client backendClientEP {
    lbMode: {
        failoverCodes : [400, 404, 502],
        interval : 0
    },
    targets: [
             {url: "http://invalidEP"},
             {url: "http://localhost:50000000"}],
    timeoutMillis:5000
    };

    http:HttpConnectorError err = {};
    http:Failover foClient = check <http:Failover>backendClientEP.getClient();
    MockClient mockClient1 = new;
    MockClient mockClient2 = new;
    http:HttpClient[] httpClients = [<http:HttpClient> mockClient1, <http:HttpClient> mockClient2];
    foClient.failoverInferredConfig.failoverClientsArray = httpClients;

    while (counter < 1) {
       http:Request request = new;
       match foClient.get("/hello", request) {
            http:Response res => {
            }
            http:HttpConnectorError httpConnectorError => {
                err = httpConnectorError;
            }
        }
        counter = counter + 1;
    }
    return err;
}

public type MockClient object {
    public {
        string serviceUri;
        http:ClientEndpointConfig config;
    }

    public function post (string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {message:"Unsupported fuction for MockClient"};
        return httpConnectorError;
    }

    public function head (string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {message:"Unsupported fuction for MockClient"};
        return httpConnectorError;
    }

    public function put (string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {message:"Unsupported fuction for MockClient"};
        return httpConnectorError;
    }

    public function execute (string httpVerb, string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {message:"Unsupported fuction for MockClient"};
        return httpConnectorError;
    }

    public function patch (string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {message:"Unsupported fuction for MockClient"};
        return httpConnectorError;
    }

    public function delete (string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {message:"Unsupported fuction for MockClient"};
        return httpConnectorError;
    }

    public function get (string path, http:Request req) returns (http:Response | http:HttpConnectorError){
        http:Response response = new;
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

    public function options (string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {message:"Unsupported fuction for MockClient"};
        return httpConnectorError;
    }

    public function forward (string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {message:"Unsupported fuction for MockClient"};
        return httpConnectorError;
    }

    public function submit (string httpVerb, string path, http:Request req) returns (http:HttpFuture | http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {message:"Unsupported fuction for MockClient"};
        return httpConnectorError;
    }

    public function getResponse (http:HttpFuture httpFuture)  returns (http:Response | http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {message:"Unsupported fuction for MockClient"};
        return httpConnectorError;
    }

    public function hasPromise (http:HttpFuture httpFuture) returns (boolean) {
        return false;
    }

    public function getNextPromise (http:HttpFuture httpFuture) returns (http:PushPromise | http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {message:"Unsupported fuction for MockClient"};
        return httpConnectorError;
    }

    public function getPromisedResponse (http:PushPromise promise) returns (http:Response | http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {message:"Unsupported fuction for MockClient"};
        return httpConnectorError;
    }

    public function rejectPromise (http:PushPromise promise) {
    }
};

function handleFailoverScenario (int count) returns (http:Response | http:HttpConnectorError) {
    if (count == 0) {
        http:HttpConnectorError err = {message:"Connection refused", statusCode:http:BAD_GATEWAY_502};
        return err;
    } else {
        http:Response inResponse = new;
        inResponse.statusCode = http:OK_200;
        return inResponse;
    }
}
