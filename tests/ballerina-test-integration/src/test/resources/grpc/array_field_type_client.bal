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

endpoint HelloWorldBlockingClient HelloWorldBlockingEp {
    url:"http://localhost:9090"
};

function testIntArrayInput(TestInt req) returns (int|string) {
    io:println("testIntArrayInput: input:");
    io:println(req);
    (int, grpc:Headers)|error unionResp = HelloWorldBlockingEp->testIntArrayInput(req);
    io:println(unionResp);
    match unionResp {
        (int, grpc:Headers) payload => {
            io:println("Client Got Response : ");
            int result;
            (result, _) = payload;
            io:println(result);
            return result;
        }
        error err => {
            io:println("Error from Connector: " + err.message);
            return "Error from Connector: " + err.message;
        }
    }
}

function testStringArrayInput(TestString req) returns (string) {
    io:println("testStringArrayInput: input:");
    io:println(req);
    (string, grpc:Headers)|error unionResp = HelloWorldBlockingEp->testStringArrayInput(req);
    io:println(unionResp);
    match unionResp {
        (string, grpc:Headers) payload => {
            io:println("Client Got Response : ");
            string result;
            (result, _) = payload;
            io:println(result);
            return result;
        }
        error err => {
            io:println("Error from Connector: " + err.message);
            return "Error from Connector: " + err.message;
        }
    }
}

function testFloatArrayInput(TestFloat req) returns (float|string) {
    io:println("testFloatArrayInput: input:");
    io:println(req);
    (float, grpc:Headers)|error unionResp = HelloWorldBlockingEp->testFloatArrayInput(req);
    io:println(unionResp);
    match unionResp {
        (float, grpc:Headers) payload => {
            io:println("Client Got Response : ");
            float result;
            (result, _) = payload;
            io:println(result);
            return result;
        }
        error err => {
            io:println("Error from Connector: " + err.message);
            return "Error from Connector: " + err.message;
        }
    }
}

function testBooleanArrayInput(TestBoolean req) returns (boolean|string) {
    io:println("testBooleanArrayInput: input:");
    io:println(req);
    (boolean, grpc:Headers)|error unionResp = HelloWorldBlockingEp->testBooleanArrayInput(req);
    io:println(unionResp);
    match unionResp {
        (boolean, grpc:Headers) payload => {
            io:println("Client Got Response : ");
            boolean result;
            (result, _) = payload;
            io:println(result);
            return result;
        }
        error err => {
            io:println("Error from Connector: " + err.message);
            return "Error from Connector: " + err.message;
        }
    }
}

function testStructArrayInput(TestStruct req) returns (string) {
    io:println("testStructArrayInput: input:");
    io:println(req);
    (string, grpc:Headers)|error unionResp = HelloWorldBlockingEp->testStructArrayInput(req);
    io:println(unionResp);
    match unionResp {
        (string, grpc:Headers) payload => {
            io:println("Client Got Response : ");
            string result;
            (result, _) = payload;
            io:println(result);
            return result;
        }
        error err => {
            io:println("Error from Connector: " + err.message);
            return "Error from Connector: " + err.message;
        }
    }
}

function testIntArrayOutput() returns (TestInt|string) {
    io:println("testIntArrayOutput: No input:");
    (TestInt, grpc:Headers)|error unionResp = HelloWorldBlockingEp->testIntArrayOutput();
    io:println(unionResp);
    match unionResp {
        (TestInt, grpc:Headers) payload => {
            io:println("Client Got Response : ");
            TestInt result;
            (result, _) = payload;
            io:println(result);
            return result;
        }
        error err => {
            io:println("Error from Connector: " + err.message);
            return "Error from Connector: " + err.message;
        }
    }
}

function testStringArrayOutput() returns (TestString|string) {
    io:println("testStringArrayOutput: No input:");
    (TestString, grpc:Headers)|error unionResp = HelloWorldBlockingEp->testStringArrayOutput();
    io:println(unionResp);
    match unionResp {
        (TestString, grpc:Headers) payload => {
            io:println("Client Got Response : ");
            TestString result;
            (result, _) = payload;
            io:println(result);
            return result;
        }
        error err => {
            io:println("Error from Connector: " + err.message);
            return "Error from Connector: " + err.message;
        }
    }
}

function testFloatArrayOutput() returns (TestFloat|string) {
    io:println("testFloatArrayOutput: No input:");
    (TestFloat, grpc:Headers)|error unionResp = HelloWorldBlockingEp->testFloatArrayOutput();
    io:println(unionResp);
    match unionResp {
        (TestFloat, grpc:Headers) payload => {
            io:println("Client Got Response : ");
            TestFloat result;
            (result, _) = payload;
            io:println(result);
            return result;
        }
        error err => {
            io:println("Error from Connector: " + err.message);
            return "Error from Connector: " + err.message;
        }
    }
}

function testBooleanArrayOutput() returns (TestBoolean|string) {
    io:println("testBooleanArrayOutput: No input:");
    (TestBoolean, grpc:Headers)|error unionResp = HelloWorldBlockingEp->testBooleanArrayOutput();
    io:println(unionResp);
    match unionResp {
        (TestBoolean, grpc:Headers) payload => {
            io:println("Client Got Response : ");
            TestBoolean result;
            (result, _) = payload;
            io:println(result);
            return result;
        }
        error err => {
            io:println("Error from Connector: " + err.message);
            return "Error from Connector: " + err.message;
        }
    }
}

function testStructArrayOutput() returns (TestStruct|string) {
    io:println("testStructArrayOutput: No input:");
    (TestStruct, grpc:Headers)|error unionResp = HelloWorldBlockingEp->testStructArrayOutput();
    io:println(unionResp);
    match unionResp {
        (TestStruct, grpc:Headers) payload => {
            io:println("Client Got Response : ");
            TestStruct result;
            (result, _) = payload;
            io:println(result);
            return result;
        }
        error err => {
            io:println("Error from Connector: " + err.message);
            return "Error from Connector: " + err.message;
        }
    }
}

public type HelloWorldBlockingStub object {

    public {
        grpc:Client clientEndpoint;
        grpc:Stub stub;
    }

    function initStub(grpc:Client clientEndpoint) {
        grpc:Stub navStub = new;
        navStub.initStub(clientEndpoint, "blocking", DESCRIPTOR_KEY, descriptorMap);
        self.stub = navStub;
    }

    function testIntArrayInput(TestInt req, grpc:Headers? headers = ()) returns ((int, grpc:Headers)|error) {
        var unionResp = self.stub.blockingExecute("HelloWorld/testIntArrayInput", req, headers);
        match unionResp {
            error payloadError => {
                return payloadError;
            }
            (any, grpc:Headers) payload => {
                any result;
                grpc:Headers resHeaders;
                (result, resHeaders) = payload;
                return (<int>result, resHeaders);
            }
        }
    }

    function testStringArrayInput(TestString req, grpc:Headers? headers = ()) returns ((string, grpc:Headers)|error) {
        var unionResp = self.stub.blockingExecute("HelloWorld/testStringArrayInput", req, headers);
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

    function testFloatArrayInput(TestFloat req, grpc:Headers? headers = ()) returns ((float, grpc:Headers)|error) {
        var unionResp = self.stub.blockingExecute("HelloWorld/testFloatArrayInput", req, headers);
        match unionResp {
            error payloadError => {
                return payloadError;
            }
            (any, grpc:Headers) payload => {
                any result;
                grpc:Headers resHeaders;
                (result, resHeaders) = payload;
                return (<float>result, resHeaders);
            }
        }
    }

    function testBooleanArrayInput(TestBoolean req, grpc:Headers? headers = ()) returns ((boolean, grpc:Headers)|error) {
        var unionResp = self.stub.blockingExecute("HelloWorld/testBooleanArrayInput", req, headers);
        match unionResp {
            error payloadError => {
                return payloadError;
            }
            (any, grpc:Headers) payload => {
                any result;
                grpc:Headers resHeaders;
                (result, resHeaders) = payload;
                return (<boolean>result, resHeaders);
            }
        }
    }

    function testStructArrayInput(TestStruct req, grpc:Headers? headers = ()) returns ((string, grpc:Headers)|error) {
        var unionResp = self.stub.blockingExecute("HelloWorld/testStructArrayInput", req, headers);
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

    function testIntArrayOutput(grpc:Headers? headers = ()) returns ((TestInt, grpc:Headers)|error) {
        Empty req = {};
        var unionResp = self.stub.blockingExecute("HelloWorld/testIntArrayOutput", req, headers);
        match unionResp {
            error payloadError => {
                return payloadError;
            }
            (any, grpc:Headers) payload => {
                any result;
                grpc:Headers resHeaders;
                (result, resHeaders) = payload;
                return (<TestInt>result, resHeaders);
            }
        }
    }

    function testStringArrayOutput(grpc:Headers? headers = ()) returns ((TestString, grpc:Headers)|error) {
        Empty req = {};
        var unionResp = self.stub.blockingExecute("HelloWorld/testStringArrayOutput", req, headers);
        match unionResp {
            error payloadError => {
                return payloadError;
            }
            (any, grpc:Headers) payload => {
                any result;
                grpc:Headers resHeaders;
                (result, resHeaders) = payload;
                return (<TestString>result, resHeaders);
            }
        }
    }

    function testFloatArrayOutput(grpc:Headers? headers = ()) returns ((TestFloat, grpc:Headers)|error) {
        Empty req = {};
        var unionResp = self.stub.blockingExecute("HelloWorld/testFloatArrayOutput", req, headers);
        match unionResp {
            error payloadError => {
                return payloadError;
            }
            (any, grpc:Headers) payload => {
                any result;
                grpc:Headers resHeaders;
                (result, resHeaders) = payload;
                return (<TestFloat>result, resHeaders);
            }
        }
    }

    function testBooleanArrayOutput(grpc:Headers? headers = ()) returns ((TestBoolean, grpc:Headers)|error) {
        Empty req = {};
        var unionResp = self.stub.blockingExecute("HelloWorld/testBooleanArrayOutput", req, headers);
        match unionResp {
            error payloadError => {
                return payloadError;
            }
            (any, grpc:Headers) payload => {
                any result;
                grpc:Headers resHeaders;
                (result, resHeaders) = payload;
                return (<TestBoolean>result, resHeaders);
            }
        }
    }

    function testStructArrayOutput(grpc:Headers? headers = ()) returns ((TestStruct, grpc:Headers)|error) {
        Empty req = {};
        var unionResp = self.stub.blockingExecute("HelloWorld/testStructArrayOutput", req, headers);
        match unionResp {
            error payloadError => {
                return payloadError;
            }
            (any, grpc:Headers) payload => {
                any result;
                grpc:Headers resHeaders;
                (result, resHeaders) = payload;
                return (<TestStruct>result, resHeaders);
            }
        }
    }

};

public type HelloWorldStub object {

    public {
        grpc:Client clientEndpoint;
        grpc:Stub stub;
    }

    function initStub(grpc:Client clientEndpoint) {
        grpc:Stub navStub = new;
        navStub.initStub(clientEndpoint, "non-blocking", DESCRIPTOR_KEY, descriptorMap);
        self.stub = navStub;
    }

    function testIntArrayInput(TestInt req, typedesc listener, grpc:Headers? headers = ()) returns (error?) {
        return self.stub.nonBlockingExecute("HelloWorld/testIntArrayInput", req, listener, headers);
    }

    function testStringArrayInput(TestString req, typedesc listener, grpc:Headers? headers = ()) returns (error?) {
        return self.stub.nonBlockingExecute("HelloWorld/testStringArrayInput", req, listener, headers);
    }

    function testFloatArrayInput(TestFloat req, typedesc listener, grpc:Headers? headers = ()) returns (error?) {
        return self.stub.nonBlockingExecute("HelloWorld/testFloatArrayInput", req, listener, headers);
    }

    function testBooleanArrayInput(TestBoolean req, typedesc listener, grpc:Headers? headers = ()) returns (error?) {
        return self.stub.nonBlockingExecute("HelloWorld/testBooleanArrayInput", req, listener, headers);
    }

    function testStructArrayInput(TestStruct req, typedesc listener, grpc:Headers? headers = ()) returns (error?) {
        return self.stub.nonBlockingExecute("HelloWorld/testStructArrayInput", req, listener, headers);
    }

    function testIntArrayOutput(typedesc listener, grpc:Headers? headers = ()) returns (error?) {
        Empty req = {};
        return self.stub.nonBlockingExecute("HelloWorld/testIntArrayOutput", req, listener, headers);
    }

    function testStringArrayOutput(typedesc listener, grpc:Headers? headers = ()) returns (error?) {
        Empty req = {};
        return self.stub.nonBlockingExecute("HelloWorld/testStringArrayOutput", req, listener, headers);
    }

    function testFloatArrayOutput(typedesc listener, grpc:Headers? headers = ()) returns (error?) {
        Empty req = {};
        return self.stub.nonBlockingExecute("HelloWorld/testFloatArrayOutput", req, listener, headers);
    }

    function testBooleanArrayOutput(typedesc listener, grpc:Headers? headers = ()) returns (error?) {
        Empty req = {};
        return self.stub.nonBlockingExecute("HelloWorld/testBooleanArrayOutput", req, listener, headers);
    }

    function testStructArrayOutput(typedesc listener, grpc:Headers? headers = ()) returns (error?) {
        Empty req = {};
        return self.stub.nonBlockingExecute("HelloWorld/testStructArrayOutput", req, listener, headers);
    }

};


public type HelloWorldBlockingClient object {

    public {
        grpc:Client client;
        HelloWorldBlockingStub stub;
    }

    public function init(grpc:ClientEndpointConfig config) {
        // initialize client endpoint.
        grpc:Client client = new;
        client.init(config);
        self.client = client;
        // initialize service stub.
        HelloWorldBlockingStub stub = new;
        stub.initStub(client);
        self.stub = stub;
    }

    public function getCallerActions() returns (HelloWorldBlockingStub) {
        return self.stub;
    }
};

public type HelloWorldClient object {

    public {
        grpc:Client client;
        HelloWorldStub stub;
    }

    public function init(grpc:ClientEndpointConfig config) {
        // initialize client endpoint.
        grpc:Client client = new;
        client.init(config);
        self.client = client;
        // initialize service stub.
        HelloWorldStub stub = new;
        stub.initStub(client);
        self.stub = stub;
    }

    public function getCallerActions() returns (HelloWorldStub) {
        return self.stub;
    }
};


type TestInt {
    int[] values;

};

type TestString {
    string[] values;

};

type TestFloat {
    float[] values;

};

type TestBoolean {
    boolean[] values;

};

type TestStruct {
    A[] values;

};

type A {
    string name;

};

type Empty {

};


@final string DESCRIPTOR_KEY = "HelloWorld.proto";
map descriptorMap =
{
    "HelloWorld.proto":
    "0A1048656C6C6F576F726C642E70726F746F1A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F1A1B676F6F676C652F70726F746F6275662F656D7074792E70726F746F22210A0754657374496E7412160A0676616C756573180120032803520676616C75657322240A0A54657374537472696E6712160A0676616C756573180120032809520676616C75657322230A0954657374466C6F617412160A0676616C756573180120032802520676616C75657322250A0B54657374426F6F6C65616E12160A0676616C756573180120032808520676616C75657322280A0A54657374537472756374121A0A0676616C75657318012003280B32022E41520676616C75657322170A014112120A046E616D6518012001280952046E616D653281050A0A48656C6C6F576F726C64123A0A1174657374496E744172726179496E70757412082E54657374496E741A1B2E676F6F676C652E70726F746F6275662E496E74363456616C756512410A1474657374537472696E674172726179496E707574120B2E54657374537472696E671A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C7565123E0A1374657374466C6F61744172726179496E707574120A2E54657374466C6F61741A1B2E676F6F676C652E70726F746F6275662E466C6F617456616C756512410A1574657374426F6F6C65616E4172726179496E707574120C2E54657374426F6F6C65616E1A1A2E676F6F676C652E70726F746F6275662E426F6F6C56616C756512410A14746573745374727563744172726179496E707574120B2E546573745374727563741A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C756512360A1274657374496E7441727261794F757470757412162E676F6F676C652E70726F746F6275662E456D7074791A082E54657374496E74123C0A1574657374537472696E6741727261794F757470757412162E676F6F676C652E70726F746F6275662E456D7074791A0B2E54657374537472696E67123A0A1474657374466C6F617441727261794F757470757412162E676F6F676C652E70726F746F6275662E456D7074791A0A2E54657374466C6F6174123E0A1674657374426F6F6C65616E41727261794F757470757412162E676F6F676C652E70726F746F6275662E456D7074791A0C2E54657374426F6F6C65616E123C0A157465737453747275637441727261794F757470757412162E676F6F676C652E70726F746F6275662E456D7074791A0B2E54657374537472756374620670726F746F33"
    ,

    "google.protobuf.wrappers.proto":
    "0A0E77726170706572732E70726F746F120F676F6F676C652E70726F746F62756622230A0B446F75626C6556616C756512140A0576616C7565180120012801520576616C756522220A0A466C6F617456616C756512140A0576616C7565180120012802520576616C756522220A0A496E74363456616C756512140A0576616C7565180120012803520576616C756522230A0B55496E74363456616C756512140A0576616C7565180120012804520576616C756522220A0A496E74333256616C756512140A0576616C7565180120012805520576616C756522230A0B55496E74333256616C756512140A0576616C756518012001280D520576616C756522210A09426F6F6C56616C756512140A0576616C7565180120012808520576616C756522230A0B537472696E6756616C756512140A0576616C7565180120012809520576616C756522220A0A427974657356616C756512140A0576616C756518012001280C520576616C756542570A13636F6D2E676F6F676C652E70726F746F627566420D577261707065727350726F746F50015A057479706573F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33"
    ,

    "google.protobuf.empty.proto":
    "0A0B656D7074792E70726F746F120F676F6F676C652E70726F746F62756622070A05456D70747942540A13636F6D2E676F6F676C652E70726F746F627566420A456D70747950726F746F50015A057479706573F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33"

};
