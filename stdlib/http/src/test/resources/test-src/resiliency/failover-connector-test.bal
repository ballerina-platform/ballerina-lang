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

function testSuccessScenario () returns @tainted http:Response | error {

    http:FailoverClient backendClientEP = new({
        failoverCodes : [400, 500, 502, 503],
        targets: [
                 {url: "http://invalidEP"},
                 {url: "http://localhost:8080"}],
        timeoutInMillis:5000
    });

    http:Response clientResponse = new;
    http:Client?[] httpClients = [createMockClient("http://invalidEP"),
                                 createMockClient("http://localhost:8080")];
    backendClientEP.failoverInferredConfig.failoverClientsArray = httpClients;

    while (counter < 2) {
        http:Request request = new;
        var serviceResponse = backendClientEP->get("/hello", request);
        if (serviceResponse is http:Response) {
            clientResponse = serviceResponse;
        } else {
            // Ignore the error to verify failover scenario
        }
        counter = counter + 1;
    }
    return clientResponse;
}

function testFailureScenario () returns @tainted http:Response | error {
    http:FailoverClient backendClientEP = new({
        failoverCodes : [400, 404, 500, 502, 503],
        targets: [
                 {url: "http://invalidEP"},
                 {url: "http://localhost:50000000"}],
        timeoutInMillis:5000
    });

    http:Response response = new;
    http:Client?[] httpClients = [createMockClient("http://invalidEP"),
                                 createMockClient("http://localhost:50000000")];
    backendClientEP.failoverInferredConfig.failoverClientsArray = httpClients;
    while (counter < 1) {
        http:Request request = new;
        var serviceResponse = backendClientEP->get("/hello", request);
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

public client class MockClient {
    public string url = "";
    public http:ClientConfiguration config = {};
    public http:Client httpClient;
    public http:CookieStore? cookieStore = ();

    public function init(string url, http:ClientConfiguration? config = ()) {
        http:Client simpleClient = new(url);
        self.url = url;
        self.config = config ?: {};
        self.cookieStore = ();
        self.httpClient = simpleClient;
    }

    public remote function post(string path,
                           http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|() message)
                                                                                returns http:Response|http:ClientError {
        return getUnsupportedError();
    }

    public remote function head(string path,
                           http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|() message = ())
                                                                                returns http:Response|http:ClientError {
        return getUnsupportedError();
    }

    public remote function put(string path,
                               http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|() message)
                                                                                returns http:Response|http:ClientError {
        return getUnsupportedError();
    }

    public remote function execute(string httpVerb, string path,
                                   http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                        message) returns http:Response|http:ClientError {
        return getUnsupportedError();
    }

    public remote function patch(string path,
                           http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|() message)
                                                                                returns http:Response|http:ClientError {
        return getUnsupportedError();
    }

    public remote function delete(string path,
                           http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|() message)
                                                                                returns http:Response|http:ClientError {
        return getUnsupportedError();
    }

    public remote function get(string path,
                            http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|() message)
                                                                                returns http:Response|http:ClientError {
        http:Response response = new;
        var result = handleFailoverScenario(counter);
        if (result is http:Response) {
            response = result;
        } else {
            string errMessage = result.message();
            response.statusCode = http:STATUS_INTERNAL_SERVER_ERROR;
            response.setTextPayload(errMessage);
        }
        return response;
    }

    public remote function options(string path,
           http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|() message = ())
                                                                                returns http:Response|http:ClientError {
        return getUnsupportedError();
    }

    public remote function forward(string path, http:Request req) returns http:Response|http:ClientError {
        return getUnsupportedError();
    }

    public remote function submit(string httpVerb, string path,
                           http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|() message)
                                                                            returns http:HttpFuture|http:ClientError {
        return getUnsupportedError();
    }

    public remote function getResponse(http:HttpFuture httpFuture)  returns http:Response|http:ClientError {
        return getUnsupportedError();
    }

    public remote function hasPromise(http:HttpFuture httpFuture) returns boolean {
        return false;
    }

    public remote function getNextPromise(http:HttpFuture httpFuture) returns http:PushPromise|http:ClientError {
        return getUnsupportedError();
    }

    public remote function getPromisedResponse(http:PushPromise promise) returns http:Response|http:ClientError {
        return getUnsupportedError();
    }

    public remote function rejectPromise(http:PushPromise promise) {
    }

    public function getCookieStore() returns http:CookieStore? {
        return self.cookieStore;
    }
}

function handleFailoverScenario (int count) returns (http:Response | http:ClientError) {
    if (count == 0) {
        return http:GenericClientError("Connection refused");
    } else {
        http:Response inResponse = new;
        inResponse.statusCode = http:STATUS_OK;
        return inResponse;
    }
}

function getUnsupportedError() returns http:ClientError {
    return http:GenericClientError("Unsupported fucntion for MockClient");
}

function createMockClient(string url) returns MockClient {
    MockClient mockClient = new(url);
    return mockClient;
}
