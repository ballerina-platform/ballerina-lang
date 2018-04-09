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

string[] responses;
int total = 0;
function testUnaryNonBlockingClient () returns (string []) {
    // Client endpoint configuration
    endpoint HelloWorldClient helloWorldEp {
        host:"localhost",
        port:9090
    };
    // Executing unary non-blocking call registering server message listener.
    error? result = helloWorldEp -> hello("WSO2", typeof HelloWorldMessageListener);
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
service<grpc:Listener> HelloWorldMessageListener {

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

public type HelloWorldBlockingStub object {
    public {
        grpc:Client clientEndpoint;
        grpc:ServiceStub serviceStub;
    }

    function initStub (grpc:Client clientEndpoint) {
        grpc:ServiceStub navStub = new;
        navStub.initStub(clientEndpoint, "blocking", DESCRIPTOR_KEY, descriptorMap);
        self.serviceStub = navStub;
    }

    function hello (string req) returns (string|error) {
        any|grpc:ConnectorError unionResp = self.serviceStub.blockingExecute("HelloWorld/hello", req);
        match unionResp {
            grpc:ConnectorError payloadError => {
                error err = {message:payloadError.message};
                return err;
            }
            any payload => {
                return <string> payload;
            }
        }
    }

    function testInt (int req) returns (int|error) {
        any|grpc:ConnectorError unionResp = self.serviceStub.blockingExecute("HelloWorld/testInt", req);
        match unionResp {
            grpc:ConnectorError payloadError => {
                error err = {message:payloadError.message};
                return err;
            }
            any payload => {
                return <int> payload;
            }
        }
    }

    function testFloat (float req) returns (float|error) {
        any|grpc:ConnectorError unionResp = self.serviceStub.blockingExecute("HelloWorld/testFloat", req);
        match unionResp {
            grpc:ConnectorError payloadError => {
                error err = {message:payloadError.message};
                return err;
            }
            any payload => {
                return <float> payload;
            }
        }
    }

    function testBoolean (boolean req) returns (boolean|error) {
        any|grpc:ConnectorError unionResp = self.serviceStub.blockingExecute("HelloWorld/testBoolean", req);
        match unionResp {
            grpc:ConnectorError payloadError => {
                error err = {message:payloadError.message};
                return err;
            }
            any payload => {
                return <boolean> payload;
            }
        }
    }

    function testStruct (Request req) returns (Response|error) {
        any|grpc:ConnectorError unionResp = self.serviceStub.blockingExecute("HelloWorld/testStruct", req);
        match unionResp {
            grpc:ConnectorError payloadError => {
                error err = {message:payloadError.message};
                return err;
            }
            any payload => {
                return <Response> payload;
            }
        }
    }
};


public type HelloWorldStub object {
    public {
        grpc:Client clientEndpoint;
        grpc:ServiceStub serviceStub;
    }

    function initStub (grpc:Client clientEndpoint) {
        grpc:ServiceStub navStub = new;
        navStub.initStub(clientEndpoint, "non-blocking", DESCRIPTOR_KEY, descriptorMap);
        self.serviceStub = navStub;
    }

    function hello (string req, typedesc listener) returns (error?) {
        var err1 = self.serviceStub.nonBlockingExecute("HelloWorld/hello", req, listener);
        if (err1 != ()) {
            error err = {message:err1.message};
            return err;
        }
        return ();
    }

    function testInt (int req, typedesc listener) returns (error?) {
        var err1 = self.serviceStub.nonBlockingExecute("HelloWorld/testInt", req, listener);
        if (err1 != ()) {
            error err = {message:err1.message};
            return err;
        }
        return ();
    }

    function testFloat (float req, typedesc listener) returns (error?) {
        var err1 = self.serviceStub.nonBlockingExecute("HelloWorld/testFloat", req, listener);
        if (err1 != ()) {
            error err = {message:err1.message};
            return err;
        }
        return ();
    }

    function testBoolean (boolean req, typedesc listener) returns (error?) {
        var err1 = self.serviceStub.nonBlockingExecute("HelloWorld/testBoolean", req, listener);
        if (err1 != ()) {
            error err = {message:err1.message};
            return err;
        }
        return ();
    }

    function testStruct (Request req, typedesc listener) returns (error?) {
        var err1 = self.serviceStub.nonBlockingExecute("HelloWorld/testStruct", req, listener);
        if (err1 != ()) {
            error err = {message:err1.message};
            return err;
        }
        return ();
    }
};


public type HelloWorldBlockingClient object {
    public {
        grpc:Client client;
        HelloWorldBlockingStub stub;
    }

    public function init (grpc:ClientEndpointConfiguration config) {
        // initialize client endpoint.
        grpc:Client client = new;
        client.init(config);
        self.client = client;
        // initialize service stub.
        HelloWorldBlockingStub stub = new;
        stub.initStub(client);
        self.stub = stub;
    }

    public function getClient () returns (HelloWorldBlockingStub) {
        return self.stub;
    }
};


public type HelloWorldClient object {
    public {
        grpc:Client client;
        HelloWorldStub stub;
    }

    public function init (grpc:ClientEndpointConfiguration config) {
        // initialize client endpoint.
        grpc:Client client = new;
        client.init(config);
        self.client = client;
        // initialize service stub.
        HelloWorldStub stub = new;
        stub.initStub(client);
        self.stub = stub;
    }

    public function getClient () returns (HelloWorldStub) {
        return self.stub;
    }
};

@final string DESCRIPTOR_KEY = "HelloWorld.proto";
map descriptorMap =
{
    "HelloWorld.proto":"0A1048656C6C6F576F726C642E70726F746F1A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F1A1B676F6F676C652F70726F746F6275662F656D7074792E70726F746F222F0A0752657175657374120A0A046E616D6518012809120D0A076D6573736167651802280912090A036167651803280322160A08526573706F6E7365120A0A04726573701801280932E4030A0A48656C6C6F576F726C6412450A0568656C6C6F121B676F6F676C652E70726F746F6275662E537472696E6756616C75651A1B676F6F676C652E70726F746F6275662E537472696E6756616C75652800300012450A0774657374496E74121A676F6F676C652E70726F746F6275662E496E74363456616C75651A1A676F6F676C652E70726F746F6275662E496E74363456616C75652800300012470A0974657374466C6F6174121A676F6F676C652E70726F746F6275662E466C6F617456616C75651A1A676F6F676C652E70726F746F6275662E466C6F617456616C75652800300012470A0B74657374426F6F6C65616E1219676F6F676C652E70726F746F6275662E426F6F6C56616C75651A19676F6F676C652E70726F746F6275662E426F6F6C56616C75652800300012230A0A746573745374727563741207526571756573741A08526573706F6E73652800300012470A0D746573744E6F526571756573741215676F6F676C652E70726F746F6275662E456D7074791A1B676F6F676C652E70726F746F6275662E537472696E6756616C75652800300012480A0E746573744E6F526573706F6E7365121B676F6F676C652E70726F746F6275662E537472696E6756616C75651A15676F6F676C652E70726F746F6275662E456D70747928003000620670726F746F33",

    "google.protobuf.google/protobuf/wrappers.proto":"0A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F120F676F6F676C652E70726F746F627566221C0A0B446F75626C6556616C7565120D0A0576616C7565180120012801221B0A0A466C6F617456616C7565120D0A0576616C7565180120012802221B0A0A496E74363456616C7565120D0A0576616C7565180120012803221C0A0B55496E74363456616C7565120D0A0576616C7565180120012804221B0A0A496E74333256616C7565120D0A0576616C7565180120012805221C0A0B55496E74333256616C7565120D0A0576616C756518012001280D221A0A09426F6F6C56616C7565120D0A0576616C7565180120012808221C0A0B537472696E6756616C7565120D0A0576616C7565180120012809221B0A0A427974657356616C7565120D0A0576616C756518012001280C427C0A13636F6D2E676F6F676C652E70726F746F627566420D577261707065727350726F746F50015A2A6769746875622E636F6D2F676F6C616E672F70726F746F6275662F7074797065732F7772617070657273F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33",

    "google.protobuf.google/protobuf/empty.proto":"0A1B676F6F676C652F70726F746F6275662F656D7074792E70726F746F120F676F6F676C652E70726F746F62756622070A05456D70747942760A13636F6D2E676F6F676C652E70726F746F627566420A456D70747950726F746F50015A276769746875622E636F6D2F676F6C616E672F70726F746F6275662F7074797065732F656D707479F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33"

};

type Request {
    string name;
    string message;
    int age;
};

type Response {
    string resp;
};