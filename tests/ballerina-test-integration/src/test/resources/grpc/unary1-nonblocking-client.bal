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
import ballerina/io;
import ballerina/grpc;
import ballerina/runtime;

string[] responses = new;
int total = 0;
function testUnaryNonBlockingClient () returns (string []) {
    // Client endpoint configuration
    endpoint helloWorldClient helloWorldEp {
        host:"localhost",
        port:9090
    };
    // Executing unary non-blocking call registering server message listener.
    error| () result = helloWorldEp -> hello("WSO2", typeof helloWorldMessageListener);
    match result {
        error payloadError => {
            io:println("Error occured while sending event " + payloadError.message);
            responses[total] = "Error occured while sending event " + payloadError.message;
            return responses;
        }
        () => {
            io:println("Connected successfully");
        }
    }

    int wait = 0;
    while(total < 2) {
        runtime:sleepCurrentWorker(1000);
        io:println("msg count: " + total);
        if (wait > 10) {
            break;
        }
        wait++;
    }
    io:println("Client got response successfully.");
    return responses;
}

// Server Message Listener.
service<grpc:Listener> helloWorldMessageListener {

    // Resource registered to receive server messages
    onMessage (string message) {
        io:println("Response received from server: " + message);
        responses[total] = message;
        total = total + 1;
    }

    // Resource registered to receive server error messages
    onError (grpc:ServerError err) {
        if (err != null) {
            io:println("Error reported from server: " + err.message);
        }
    }

    // Resource registered to receive server completed message.
    onComplete () {
        io:println("Server Complete Sending Response.");
        responses[total] = "Server Complete Sending Response.";
        total = total + 1;
    }
}

public type helloWorldBlockingStub object {
    public {
        grpc:Client clientEndpoint;
        grpc:ServiceStub serviceStub;
    }

    function initStub (grpc:Client clientEndpoint) {
        grpc:ServiceStub navStub = new;
        navStub.initStub(clientEndpoint, "blocking", descriptorKey, descriptorMap);
        self.serviceStub = navStub;
    }

    function hello (string req) returns (string|error) {
        any|grpc:ConnectorError unionResp = self.serviceStub.blockingExecute("helloWorld/hello", req);
        match unionResp {
            grpc:ConnectorError payloadError => {
                error err = {message:payloadError.message};
                return err;
            }
            any payload => {
                string result = <string> payload;
                return result;
            }
        }
    }

    function testInt (int req) returns (int|error) {
        any|grpc:ConnectorError unionResp = self.serviceStub.blockingExecute("helloWorld/testInt", req);
        match unionResp {
            grpc:ConnectorError payloadError => {
                error err = {message:payloadError.message};
                return err;
            }
            any payload => {
                int result = <int> payload;
                return result;
            }
        }
    }

    function testFloat (float req) returns (float|error) {
        any|grpc:ConnectorError unionResp = self.serviceStub.blockingExecute("helloWorld/testFloat", req);
        match unionResp {
            grpc:ConnectorError payloadError => {
                error err = {message:payloadError.message};
                return err;
            }
            any payload => {
                float result = <float> payload;
                return result;
            }
        }
    }

    function testBoolean (boolean req) returns (boolean|error) {
        any|grpc:ConnectorError unionResp = self.serviceStub.blockingExecute("helloWorld/testBoolean", req);
        match unionResp {
            grpc:ConnectorError payloadError => {
                error err = {message:payloadError.message};
                return err;
            }
            any payload => {
                boolean result = <boolean> payload;
                return result;
            }
        }
    }

    function testStruct (Request req) returns (Response|error) {
        any|grpc:ConnectorError unionResp = self.serviceStub.blockingExecute("helloWorld/testStruct", req);
        match unionResp {
            grpc:ConnectorError payloadError => {
                error err = {message:payloadError.message};
                return err;
            }
            any payload => {
                Response result = <Response> payload;
                return result;
            }
        }
    }
}


public type helloWorldStub object {
    public {
        grpc:Client clientEndpoint;
        grpc:ServiceStub serviceStub;
    }

    function initStub (grpc:Client clientEndpoint) {
        grpc:ServiceStub navStub = new;
        navStub.initStub(clientEndpoint, "non-blocking", descriptorKey, descriptorMap);
        self.serviceStub = navStub;
    }

    function hello (string req, typedesc listener) returns (error| ()) {
        var err1 = self.serviceStub.nonBlockingExecute("helloWorld/hello", req, listener);
        if (err1 != ()) {
            error err = {message:err1.message};
            return err;
        }
        return ();
    }

    function testInt (int req, typedesc listener) returns (error| ()) {
        var err1 = self.serviceStub.nonBlockingExecute("helloWorld/testInt", req, listener);
        if (err1 != ()) {
            error err = {message:err1.message};
            return err;
        }
        return ();
    }

    function testFloat (float req, typedesc listener) returns (error| ()) {
        var err1 = self.serviceStub.nonBlockingExecute("helloWorld/testFloat", req, listener);
        if (err1 != ()) {
            error err = {message:err1.message};
            return err;
        }
        return ();
    }

    function testBoolean (boolean req, typedesc listener) returns (error| ()) {
        var err1 = self.serviceStub.nonBlockingExecute("helloWorld/testBoolean", req, listener);
        if (err1 != ()) {
            error err = {message:err1.message};
            return err;
        }
        return ();
    }

    function testStruct (Request req, typedesc listener) returns (error| ()) {
        var err1 = self.serviceStub.nonBlockingExecute("helloWorld/testStruct", req, listener);
        if (err1 != ()) {
            error err = {message:err1.message};
            return err;
        }
        return ();
    }
}


public type helloWorldBlockingClient object {
    public {
        grpc:Client client;
        helloWorldBlockingStub stub;
    }

    public function init (grpc:ClientEndpointConfiguration config) {
        // initialize client endpoint.
        grpc:Client client = new;
        client.init(config);
        self.client = client;
        // initialize service stub.
        helloWorldBlockingStub stub = new;
        stub.initStub(client);
        self.stub = stub;
    }

    public function getClient () returns (helloWorldBlockingStub) {
        return self.stub;
    }
}


public type helloWorldClient object {
    public {
        grpc:Client client;
        helloWorldStub stub;
    }

    public function init (grpc:ClientEndpointConfiguration config) {
        // initialize client endpoint.
        grpc:Client client = new;
        client.init(config);
        self.client = client;
        // initialize service stub.
        helloWorldStub stub = new;
        stub.initStub(client);
        self.stub = stub;
    }

    public function getClient () returns (helloWorldStub) {
        return self.stub;
    }
}

@final string descriptorKey = "helloWorld.proto";
map descriptorMap =
{
    "helloWorld.proto":"0A1068656C6C6F576F726C642E70726F746F1A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F22490A075265717565737412120A046E616D6518012001280952046E616D6512180A076D65737361676518022001280952076D65737361676512100A036167651803200128035203616765221E0A08526573706F6E736512120A047265737018012001280952047265737032C7020A0A68656C6C6F576F726C6412430A0568656C6C6F121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C756512430A0774657374496E74121B2E676F6F676C652E70726F746F6275662E496E74363456616C75651A1B2E676F6F676C652E70726F746F6275662E496E74363456616C756512450A0974657374466C6F6174121B2E676F6F676C652E70726F746F6275662E466C6F617456616C75651A1B2E676F6F676C652E70726F746F6275662E466C6F617456616C756512450A0B74657374426F6F6C65616E121A2E676F6F676C652E70726F746F6275662E426F6F6C56616C75651A1A2E676F6F676C652E70726F746F6275662E426F6F6C56616C756512210A0A7465737453747275637412082E526571756573741A092E526573706F6E7365620670726F746F33",

    "google.protobuf.wrappers.proto":"0A0E77726170706572732E70726F746F120F676F6F676C652E70726F746F62756622230A0B446F75626C6556616C756512140A0576616C7565180120012801520576616C756522220A0A466C6F617456616C756512140A0576616C7565180120012802520576616C756522220A0A496E74363456616C756512140A0576616C7565180120012803520576616C756522230A0B55496E74363456616C756512140A0576616C7565180120012804520576616C756522220A0A496E74333256616C756512140A0576616C7565180120012805520576616C756522230A0B55496E74333256616C756512140A0576616C756518012001280D520576616C756522210A09426F6F6C56616C756512140A0576616C7565180120012808520576616C756522230A0B537472696E6756616C756512140A0576616C7565180120012809520576616C756522220A0A427974657356616C756512140A0576616C756518012001280C520576616C756542570A13636F6D2E676F6F676C652E70726F746F627566420D577261707065727350726F746F50015A057479706573F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33"
};

type Request {
    string name;
    string message;
    int age;
}

type Response {
    string resp;
}