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

endpoint HelloWorldBlockingClient helloWorldBlockingEp {
    url:"http://localhost:9098"
};

function testInvalidRemoteMethod(string name) returns (string) {
    (string, grpc:Headers)|error unionResp = helloWorldBlockingEp->hello(name);
    match unionResp {
        (string, grpc:Headers) payload => {
            io:println("Client Got Response : ");
            string result;
            (result, _) = payload;
            io:println(result);
            return "Client got response: " + result;
        }
        error err => {
            io:println("Error from Connector: " + err.message);
            return "Error from Connector: " + err.message;
        }
    }
}

function testInvalidInputParameter(int age) returns (int) {
    (int, grpc:Headers)|error unionResp = helloWorldBlockingEp->testInt(age);
    match unionResp {
        error err => {
            io:println(err);
            return -1;
        }
        (int, grpc:Headers) payload => {
            io:println("Client got response : ");
            int result;
            (result, _) = payload;
            io:println(result);
            return result;
        }
    }
}

function testInvalidOutputResponse(float salary) returns (float|string) {
    (float, grpc:Headers)|error unionResp = helloWorldBlockingEp->testFloat(salary);
    match unionResp {
        error err => {
            string message = "Error from Connector: " + err.message;
            io:println(message);
            return message;
        }
        (float, grpc:Headers) payload => {
            io:println("Client got response : ");
            float result;
            (result, _) = payload;
            io:println(result);
            return result;
        }
    }
}

function testNonExistenceRemoteMethod(boolean isAvailable) returns (boolean|string) {
    (boolean, grpc:Headers)|error unionResp = helloWorldBlockingEp->testBoolean(isAvailable);
    match unionResp {
        error err => {
            string message = "Error from Connector: " + err.message;
            io:println(message);
            return message;
        }
        (boolean, grpc:Headers) payload => {
            io:println("Client got response : ");
            boolean result;
            (result, _) = payload;
            io:println(result);
            return result;
        }
    }
}

public type HelloWorldBlockingStub object {

    public grpc:Client clientEndpoint;
    public grpc:Stub stub;

    function initStub(grpc:Client ep) {
        grpc:Stub navStub = new;
        navStub.initStub(ep, "blocking", DESCRIPTOR_KEY, descriptorMap);
        self.stub = navStub;
    }

    function hello(string req, grpc:Headers? headers = ()) returns ((string, grpc:Headers)|error) {
        var unionResp = self.stub.blockingExecute("grpcServices.HelloWorld98/hello1", req, headers = headers);
        match unionResp {
            error payloadError => {
                return payloadError;
            }
            (any, grpc:Headers) payload => {
                any result;
                grpc:Headers resHeaders;
                (result, resHeaders) = payload;
                return (<string>result, resHeaders);
            }
        }
    }

    function testInt(int req, grpc:Headers? headers = ()) returns ((int, grpc:Headers)|error) {
        var unionResp = self.stub.blockingExecute("grpcServices.HelloWorld98/testInt", req, headers = headers);
        match unionResp {
            error payloadError => {
                return payloadError;
            }
            (any, grpc:Headers) payload => {
                any result;
                grpc:Headers resHeaders;
                (result, resHeaders) = payload;
                return (check <int>result, resHeaders);
            }
        }
    }

    function testFloat(float req, grpc:Headers? headers = ()) returns ((float, grpc:Headers)|error) {
        var unionResp = self.stub.blockingExecute("grpcServices.HelloWorld98/testFloat", req, headers = headers);
        match unionResp {
            error payloadError => {
                return payloadError;
            }
            (any, grpc:Headers) payload => {
                any result;
                grpc:Headers resHeaders;
                (result, resHeaders) = payload;
                return (check <float>result, resHeaders);
            }
        }
    }

    function testBoolean(boolean req, grpc:Headers? headers = ()) returns ((boolean, grpc:Headers)|error) {
        var unionResp = self.stub.blockingExecute("grpcServices.HelloWorld98/testBoolean", req, headers = headers);
        match unionResp {
            error payloadError => {
                return payloadError;
            }
            (any, grpc:Headers) payload => {
                any result;
                grpc:Headers resHeaders;
                (result, resHeaders) = payload;
                return (check <boolean>result, resHeaders);
            }
        }
    }
};

public type HelloWorldStub object {

    public grpc:Client clientEndpoint;
    public grpc:Stub stub;

    function initStub(grpc:Client ep) {
        grpc:Stub navStub = new;
        navStub.initStub(ep, "non-blocking", DESCRIPTOR_KEY, descriptorMap);
        self.stub = navStub;
    }

    function hello(string req, typedesc listener, grpc:Headers? headers = ()) returns (error?) {
        return self.stub.nonBlockingExecute("grpcServices.HelloWorld98/hello", req, listener, headers = headers);
    }

    function testInt(int req, typedesc listener, grpc:Headers? headers = ()) returns (error?) {
        return self.stub.nonBlockingExecute("grpcServices.HelloWorld98/testInt", req, listener, headers = headers);
    }

    function testFloat(float req, typedesc listener, grpc:Headers? headers = ()) returns (error?) {
        return self.stub.nonBlockingExecute("grpcServices.HelloWorld98/testFloat", req, listener, headers = headers);
    }

    function testBoolean(boolean req, typedesc listener, grpc:Headers? headers = ()) returns (error?) {
        return self.stub.nonBlockingExecute("grpcServices.HelloWorld98/testBoolean", req, listener, headers = headers);
    }
};

public type HelloWorldBlockingClient object {

    public grpc:Client client;
    public HelloWorldBlockingStub stub;

    public function init(grpc:ClientEndpointConfig config) {
        // initialize client endpoint.
        grpc:Client c = new;
        c.init(config);
        self.client = c;
        // initialize service stub.
        HelloWorldBlockingStub s = new;
        s.initStub(c);
        self.stub = s;
    }

    public function getCallerActions() returns (HelloWorldBlockingStub) {
        return self.stub;
    }
};

public type helloWorldClient object {

    public grpc:Client client;
    public HelloWorldStub stub;

    public function init(grpc:ClientEndpointConfig config) {
        // initialize client endpoint.
        grpc:Client c = new;
        c.init(config);
        self.client = c;
        // initialize service stub.
        HelloWorldStub s = new;
        s.initStub(c);
        self.stub = s;
    }

    public function getCallerActions() returns (HelloWorldStub) {
        return self.stub;
    }
};

@final string DESCRIPTOR_KEY = "grpcServices.HelloWorld98.proto";
map descriptorMap =
{
    "grpcServices.HelloWorld98.proto":"0A1248656C6C6F576F726C6439382E70726F746F120C6772706353657276696365731A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F32E1010A0C48656C6C6F576F726C64393812430A0568656C6C6F121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C756512440A0774657374496E74121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A1B2E676F6F676C652E70726F746F6275662E496E74363456616C756512460A0974657374466C6F6174121B2E676F6F676C652E70726F746F6275662E466C6F617456616C75651A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C7565620670726F746F33",

    "google.protobuf.google/protobuf/wrappers.proto":
    "0A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F120F676F6F676C652E70726F746F627566221C0A0B446F75626C6556616C7565120D0A0576616C7565180120012801221B0A0A466C6F617456616C7565120D0A0576616C7565180120012802221B0A0A496E74363456616C7565120D0A0576616C7565180120012803221C0A0B55496E74363456616C7565120D0A0576616C7565180120012804221B0A0A496E74333256616C7565120D0A0576616C7565180120012805221C0A0B55496E74333256616C7565120D0A0576616C756518012001280D221A0A09426F6F6C56616C7565120D0A0576616C7565180120012808221C0A0B537472696E6756616C7565120D0A0576616C7565180120012809221B0A0A427974657356616C7565120D0A0576616C756518012001280C427C0A13636F6D2E676F6F676C652E70726F746F627566420D577261707065727350726F746F50015A2A6769746875622E636F6D2F676F6C616E672F70726F746F6275662F7074797065732F7772617070657273F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33"
    ,

    "google.protobuf.google/protobuf/empty.proto":
    "0A1B676F6F676C652F70726F746F6275662F656D7074792E70726F746F120F676F6F676C652E70726F746F62756622070A05456D70747942760A13636F6D2E676F6F676C652E70726F746F627566420A456D70747950726F746F50015A276769746875622E636F6D2F676F6C616E672F70726F746F6275662F7074797065732F656D707479F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33"

};

type Request record {
    string name;
    string message;
    int age;
};

type Response record {
    string resp;
};
