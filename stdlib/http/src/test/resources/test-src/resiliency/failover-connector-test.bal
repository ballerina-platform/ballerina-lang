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
import ballerina/mime;

int counter = 0;

function testSuccessScenario () returns (http:Response | error) {

    http:FailoverClient backendClientEP = new({
        failoverCodes : [400, 500, 502, 503],
        targets: [
                 {url: "http://invalidEP"},
                 {url: "http://localhost:8080"}],
        timeoutMillis:5000
    });

    http:Response clientResponse = new;
    http:Client?[] httpClients = [createMockClient("http://invalidEP"),
                                 createMockClient("http://localhost:8080")];
    backendClientEP.failoverInferredConfig.failoverClientsArray = httpClients;

    while (counter < 2) {
        http:Request request = new;
        var serviceResponse = backendClientEP->get("/hello", message = request);
        if (serviceResponse is http:Response) {
            clientResponse = serviceResponse;
        } else {
            // Ignore the error to verify failover scenario
        }
        counter = counter + 1;
    }
    return clientResponse;
}

function testFailureScenario () returns (http:Response | error) {
    http:FailoverClient backendClientEP = new({
        failoverCodes : [400, 404, 500, 502, 503],
        targets: [
                 {url: "http://invalidEP"},
                 {url: "http://localhost:50000000"}],
        timeoutMillis:5000
    });

    http:Response response = new;
    http:Client?[] httpClients = [createMockClient("http://invalidEP"),
                                 createMockClient("http://localhost:50000000")];
    backendClientEP.failoverInferredConfig.failoverClientsArray = httpClients;
    while (counter < 1) {
        http:Request request = new;
        var serviceResponse = backendClientEP->get("/hello", message = request);
        if (serviceResponse is http:Response) {
            counter = counter + 1;
            response = serviceResponse;
        } else {
            counter = counter + 1;
            return serviceResponse;
        }
    }
    return response;
}

public type MockClient client object {
    public string url = "";
    public http:ClientEndpointConfig config = {};
    public http:Client httpClient;

    public function __init(string url, http:ClientEndpointConfig? config = ()) {
        self.url = url;
        self.config = config ?: {};
        http:Client simpleClient = new(url);
        self.httpClient = simpleClient;
    }

    public remote function post(string path,
                           http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|() message)
                                                                                        returns http:Response|error {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public remote function head(string path,
                           http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|() message = ())
                                                                                        returns http:Response|error {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public remote function put(string path,
                               http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|() message)
                                                                                        returns http:Response|error {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public remote function execute(string httpVerb, string path,
                                   http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                        message) returns http:Response|error {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public remote function patch(string path,
                           http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|() message)
                                                                                        returns http:Response|error {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public remote function delete(string path,
                           http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|() message)
                                                                                        returns http:Response|error {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public remote function get(string path, http:Request req) returns http:Response|error {
        http:Response response = new;
        var result = handleFailoverScenario(counter);
        if (result is http:Response) {
            response = result;
        } else {
            string errMessage = result.reason();
            response.statusCode = http:INTERNAL_SERVER_ERROR_500;
            response.setTextPayload(errMessage);
        }
        return response;
    }

    public remote function options(string path,
           http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|() message = ())
                                                                                        returns http:Response|error {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public remote function forward(string path, http:Request req) returns http:Response|error {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public remote function submit(string httpVerb, string path,
                           http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|() message)
                                                                                        returns http:HttpFuture|error {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public remote function getResponse(http:HttpFuture httpFuture)  returns http:Response|error {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public remote function hasPromise(http:HttpFuture httpFuture) returns boolean {
        return false;
    }

    public remote function getNextPromise(http:HttpFuture httpFuture) returns http:PushPromise|error {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public remote function getPromisedResponse(http:PushPromise promise) returns http:Response|error {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public remote function rejectPromise(http:PushPromise promise) {
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

function createMockClient(string url) returns MockClient {
    MockClient mockClient = new(url);
    return mockClient;
}
