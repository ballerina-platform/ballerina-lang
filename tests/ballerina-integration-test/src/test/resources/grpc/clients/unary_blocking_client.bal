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
import ballerina/grpc;
import ballerina/io;


endpoint HelloWorldBlockingClient helloWorldBlockingEp {
    url:"http://localhost:9100"
};

function testUnaryBlockingClient(string name) returns (string) {
    (string, grpc:Headers)|error unionResp = helloWorldBlockingEp->hello(name);
    if (unionResp is error) {
        io:println("Error from Connector: " + unionResp.reason());
        return "Error from Connector: " + unionResp.reason();
    } else {
        io:println("Client Got Response : ");
        string result = "";
        (result, _) = unionResp;
        io:println(result);
        return "Client got response: " + result;
    }
}

function testUnaryBlockingIntClient(int age) returns (int) {
    (int, grpc:Headers)|error unionResp = helloWorldBlockingEp->testInt(age);
    if (unionResp is error) {
        io:println(unionResp.reason());
        return -1;
    } else {
        io:println("Client got response : ");
        int result = 0;
        (result, _) = unionResp;
        io:println(result);
        return result;
    }
}

function testUnaryBlockingFloatClient(float salary) returns (float) {
    (float, grpc:Headers)|error unionResp = helloWorldBlockingEp->testFloat(salary);
    if (unionResp is error) {
        io:println("Error from Connector: " + unionResp.reason());
        return -1.0;
    } else {
        io:println("Client got response : ");
        float result = 0.0;
        (result, _) = unionResp;
        io:println(result);
        return result;
    }
}

function testUnaryBlockingBoolClient(boolean isAvailable) returns (boolean) {
    (boolean, grpc:Headers)|error unionResp = helloWorldBlockingEp->testBoolean(isAvailable);
    if (unionResp is error) {
        io:println("Error from Connector: " + unionResp.reason());
        return false;
    } else {
        io:println("Client got response : ");
        boolean result = false;
        (result, _) = unionResp;
        io:println(result);
        return result;
    }
}

function testResponseInsideMatch(string msg) returns Response {
    (Response, grpc:Headers)|error unionResp = helloWorldBlockingEp->testResponseInsideMatch(msg);
    if (unionResp is error) {
        io:println("Error from Connector: " + unionResp.reason());
        return {};
    } else {
        io:println("Client got response : ");
        Response result = {};
        (result, _) = unionResp;
        io:println(result);
        return result;
    }
}

function testUnaryBlockingStructClient(Request req) returns (Response) {
    //Request req = {name:"Sam", age:25, message:"Testing."};
    (Response, grpc:Headers)|error unionResp = helloWorldBlockingEp->testStruct(req);
    if (unionResp is error) {
        io:println("Error from Connector: " + unionResp.reason());
        return {};
    } else {
        io:println("Client got response : ");
        Response result = {};
        (result, _) = unionResp;
        io:println(result);
        return result;
    }
}

public type HelloWorldBlockingStub object {

    public grpc:Client clientEndpoint = new;
    public grpc:Stub stub = new;


    function initStub(grpc:Client ep) {
        grpc:Stub navStub = new;
        error? result = navStub.initStub(ep, "blocking", DESCRIPTOR_KEY, descriptorMap);
        if (result is error) {
            panic result;
        } else {
            self.stub = navStub;
        }
    }

    function hello(string req, grpc:Headers? headers = ()) returns ((string, grpc:Headers)|error) {
        var unionResp = check self.stub.blockingExecute("grpcservices.HelloWorld100/hello", req, headers = headers);
        any result = ();
        grpc:Headers resHeaders = new;
        (result, resHeaders) = unionResp;
        return (<string>result, resHeaders);
    }

    function testInt(int req, grpc:Headers? headers = ()) returns ((int, grpc:Headers)|error) {
        var unionResp = check self.stub.blockingExecute("grpcservices.HelloWorld100/testInt", req, headers = headers);
        any result = ();
        grpc:Headers resHeaders = new;
        (result, resHeaders) = unionResp;
        return (check <int>result, resHeaders);
    }

    function testFloat(float req, grpc:Headers? headers = ()) returns ((float, grpc:Headers)|error) {
        var unionResp = check self.stub.blockingExecute("grpcservices.HelloWorld100/testFloat", req, headers = headers);
        any result = ();
        grpc:Headers resHeaders = new;
        (result, resHeaders) = unionResp;
        return (check <float>result, resHeaders);
    }

    function testBoolean(boolean req, grpc:Headers? headers = ()) returns ((boolean, grpc:Headers)|error) {
        var unionResp = check self.stub.blockingExecute("grpcservices.HelloWorld100/testBoolean", req, headers = headers);
        any result = ();
        grpc:Headers resHeaders = new;
        (result, resHeaders) = unionResp;
        return (check <boolean>result, resHeaders);
    }

    function testStruct(Request req, grpc:Headers? headers = ()) returns ((Response, grpc:Headers)|error) {
        var unionResp = check self.stub.blockingExecute("grpcservices.HelloWorld100/testStruct", req, headers = headers);
        any result = ();
        grpc:Headers resHeaders = new;
        (result, resHeaders) = unionResp;
        return (check <Response>result, resHeaders);
    }

    function testResponseInsideMatch(string req, grpc:Headers? headers = ()) returns (Response, grpc:Headers)|error {
        var unionResp = check self.stub.blockingExecute("grpcservices.HelloWorld100/testResponseInsideMatch", req,
                                                    headers = headers);
        any result = ();
        grpc:Headers resHeaders = new;
        (result, resHeaders) = unionResp;
        return (check <Response>result, resHeaders);
    }
};

public type HelloWorldStub object {

    public grpc:Client clientEndpoint = new;
    public grpc:Stub stub = new;


    function initStub(grpc:Client ep) {
        grpc:Stub navStub = new;
        error? result = navStub.initStub(ep, "non-blocking", DESCRIPTOR_KEY, descriptorMap);
        if (result is error) {
            panic result;
        } else {
            self.stub = navStub;
        }
    }

    function hello(string req, typedesc listener, grpc:Headers? headers = ()) returns (error?) {
        return self.stub.nonBlockingExecute("grpcservices.HelloWorld100/hello", req, listener, headers = headers);
    }

    function testInt(int req, typedesc listener, grpc:Headers? headers = ()) returns (error?) {
        return self.stub.nonBlockingExecute("grpcservices.HelloWorld100/testInt", req, listener, headers = headers);
    }

    function testFloat(float req, typedesc listener, grpc:Headers? headers = ()) returns (error?) {
        return self.stub.nonBlockingExecute("grpcservices.HelloWorld100/testFloat", req, listener, headers = headers);
    }

    function testBoolean(boolean req, typedesc listener, grpc:Headers? headers = ()) returns (error?) {
        return self.stub.nonBlockingExecute("grpcservices.HelloWorld100/testBoolean", req, listener, headers = headers);
    }

    function testStruct(Request req, typedesc listener, grpc:Headers? headers = ()) returns (error?) {
        return self.stub.nonBlockingExecute("grpcservices.HelloWorld100/testStruct", req, listener, headers = headers);
    }

    function testResponseInsideMatch (string req, typedesc listener, grpc:Headers? headers = ()) returns error? {
        return self.stub.nonBlockingExecute("grpcservices.HelloWorld100/testResponseInsideMatch", req, listener,
                headers = headers);
    }
};


public type HelloWorldBlockingClient object {

    public grpc:Client client = new;
    public HelloWorldBlockingStub stub = new;


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

    public grpc:Client client = new;
    public HelloWorldStub stub = new;


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

@final string DESCRIPTOR_KEY = "HelloWorld100.proto";
map descriptorMap = {
    "HelloWorld100.proto":"0A1348656C6C6F576F726C643130302E70726F746F120C6772706373657276696365731A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F1A1B676F6F676C652F70726F746F6275662F656D7074792E70726F746F22490A075265717565737412120A046E616D6518012001280952046E616D6512180A076D65737361676518022001280952076D65737361676512100A036167651803200128035203616765221E0A08526573706F6E736512120A047265737018012001280952047265737032B5030A0D48656C6C6F576F726C6431303012430A0568656C6C6F121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C756512430A0774657374496E74121B2E676F6F676C652E70726F746F6275662E496E74363456616C75651A1B2E676F6F676C652E70726F746F6275662E496E74363456616C756512450A0974657374466C6F6174121B2E676F6F676C652E70726F746F6275662E466C6F617456616C75651A1B2E676F6F676C652E70726F746F6275662E466C6F617456616C756512450A0B74657374426F6F6C65616E121A2E676F6F676C652E70726F746F6275662E426F6F6C56616C75651A1A2E676F6F676C652E70726F746F6275662E426F6F6C56616C7565123B0A0A7465737453747275637412152E6772706373657276696365732E526571756573741A162E6772706373657276696365732E526573706F6E7365124F0A1774657374526573706F6E7365496E736964654D61746368121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A162E6772706373657276696365732E526573706F6E7365620670726F746F33",
    "google/protobuf/wrappers.proto":"0A0E77726170706572732E70726F746F120F676F6F676C652E70726F746F62756622230A0B446F75626C6556616C756512140A0576616C7565180120012801520576616C756522220A0A466C6F617456616C756512140A0576616C7565180120012802520576616C756522220A0A496E74363456616C756512140A0576616C7565180120012803520576616C756522230A0B55496E74363456616C756512140A0576616C7565180120012804520576616C756522220A0A496E74333256616C756512140A0576616C7565180120012805520576616C756522230A0B55496E74333256616C756512140A0576616C756518012001280D520576616C756522210A09426F6F6C56616C756512140A0576616C7565180120012808520576616C756522230A0B537472696E6756616C756512140A0576616C7565180120012809520576616C756522220A0A427974657356616C756512140A0576616C756518012001280C520576616C756542570A13636F6D2E676F6F676C652E70726F746F627566420D577261707065727350726F746F50015A057479706573F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33",
    "google/protobuf/empty.proto":"0A0B656D7074792E70726F746F120F676F6F676C652E70726F746F62756622070A05456D70747942540A13636F6D2E676F6F676C652E70726F746F627566420A456D70747950726F746F50015A057479706573F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33"

};

type Request record {
    string name = "";
    string message = "";
    int age = 0;
};

type Response record {
    string resp = "";
};
