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
import ballerina/io;

int counter = 0;

function testSuccessScenario () returns (http:Response | error) {

    endpoint http:FailoverClient backendClientEP {
        failoverCodes : [400, 500, 502, 503],
        targets: [
                 {url: "http://invalidEP"},
                 {url: "http://localhost:8080"}],
        timeoutMillis:5000
    };

    http:Response clientResponse = new;
    http:FailoverActions foClient =  backendClientEP.getCallerActions();
    MockClient mockClient1 = new;
    MockClient mockClient2 = new;
    http:CallerActions[] httpClients = [<http:CallerActions> mockClient1, <http:CallerActions> mockClient2];
    foClient.failoverInferredConfig.failoverClientsArray = httpClients;

    while (counter < 2) {
        http:Request request = new;
        var serviceResponse = foClient.get("/hello", message = request);
        if (serviceResponse is http:Response) {
            clientResponse = serviceResponse;
        } else if (serviceResponse is error) {
            // Ignore the error to verify failover scenario
        }
        counter = counter + 1;
    }
    return clientResponse;
}

function testFailureScenario () returns (http:Response | error) {
    endpoint http:FailoverClient backendClientEP {
        failoverCodes : [400, 404, 500, 502, 503],
        targets: [
                 {url: "http://invalidEP"},
                 {url: "http://localhost:50000000"}],
        timeoutMillis:5000
    };

    http:Response response = new;
    http:FailoverActions foClient = backendClientEP.getCallerActions();
    MockClient mockClient1 = new;
    MockClient mockClient2 = new;
    http:CallerActions[] httpClients = [<http:CallerActions> mockClient1, <http:CallerActions> mockClient2];
    foClient.failoverInferredConfig.failoverClientsArray = httpClients;
    while (counter < 1) {
        http:Request request = new;
        var serviceResponse = foClient.get("/hello", message = request);
        if (serviceResponse is http:Response) {
            counter = counter + 1;
            response = serviceResponse;
        } else if (serviceResponse is error) {
            counter = counter + 1;
            return serviceResponse;
        }
    }
    return response;
}

public type MockClient object {
    public string serviceUri = "";
    public http:ClientEndpointConfig config = {};

    public function post (string path, http:Request req) returns (http:Response | error) {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public function head (string path, http:Request req) returns (http:Response | error) {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public function put (string path, http:Request req) returns (http:Response | error) {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public function execute (string httpVerb, string path, http:Request req) returns (http:Response | error) {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public function patch (string path, http:Request req) returns (http:Response | error) {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public function delete (string path, http:Request req) returns (http:Response | error) {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public function get (string path, http:Request req) returns (http:Response | error){
        http:Response response = new;
        match  handleFailoverScenario(counter) {
            http:Response res => {
                response = res;
            }
            error httpConnectorError => {
                string message = httpConnectorError.reason();
                response.statusCode = http:INTERNAL_SERVER_ERROR_500;
                response.setTextPayload(message);
            }
        }
        return response;
    }

    public function options (string path, http:Request req) returns (http:Response | error) {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public function forward (string path, http:Request req) returns (http:Response | error) {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public function submit (string httpVerb, string path, http:Request req) returns (http:HttpFuture | error) {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public function getResponse (http:HttpFuture httpFuture)  returns (http:Response | error) {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public function hasPromise (http:HttpFuture httpFuture) returns (boolean) {
        return false;
    }

    public function getNextPromise (http:HttpFuture httpFuture) returns (http:PushPromise | error) {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public function getPromisedResponse (http:PushPromise promise) returns (http:Response | error) {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public function rejectPromise (http:PushPromise promise) {
    }
};

function handleFailoverScenario (int count) returns (http:Response | error) {
    if (count == 0) {
        error err = error("Connection refused");
        return err;
    } else {
        http:Response inResponse = new;
        inResponse.statusCode = http:OK_200;
        return inResponse;
    }
}
