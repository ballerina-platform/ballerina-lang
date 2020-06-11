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
import ballerina/runtime;

const string ERROR_MSG_FORMAT = "Error from Connector: %s";
boolean respReceived = false;
boolean eofReceived = false;

// Enable when you need to test locally.
//public function main() {
//    boolean a = testUnaryNonBlockingClient();
//    io:println(a);
//}

function testUnaryNonBlockingClient() returns boolean {
    // Client endpoint configuration
    HelloWorldClient helloWorldEp = new ("http://localhost:9097");
    // Executing unary non-blocking call registering server message listener.
    grpc:Error? result = helloWorldEp->hello("WSO2", HelloWorldMessageListener);
    if (result is grpc:Error) {
        string msg = io:sprintf(ERROR_MSG_FORMAT, result.message());
        io:println(msg);
        return false;
    } else {
        io:println("Connected successfully");
    }

    int waitCount = 0;
    while(true) {
        if (respReceived && eofReceived) {
            break;
        }
        runtime:sleep(1000);
        io:println("response received: ", respReceived);
        io:println("EOF received: ", eofReceived);
        if (waitCount > 10) {
            break;
        }
        waitCount += 1;
    }
    boolean eof = (respReceived && eofReceived);
    io:println("Response received: " + eof.toString());
    return (respReceived && eofReceived);
}

// Server Message Listener.
service HelloWorldMessageListener = service {

    // Resource registered to receive server messages
    resource function onMessage(string message) {
        io:println("Response received from server: " + message);
        respReceived = true;
    }

    // Resource registered to receive server error messages
    resource function onError(error err) {
        string msg = io:sprintf(ERROR_MSG_FORMAT, err.message());
        io:println(msg);
    }

    // Resource registered to receive server completed message.
    resource function onComplete() {
        io:println("Server Complete Sending Response.");
        eofReceived = true;
    }
};

public type HelloWorldBlockingClient client object {

    *grpc:AbstractClientEndpoint;

    private grpc:Client grpcClient;

    public function __init(string url, grpc:ClientConfiguration? config = ()) {
        // initialize client endpoint.
        self.grpcClient = new(url, config);
        checkpanic self.grpcClient.initStub(self, "blocking", ROOT_DESCRIPTOR, getDescriptorMap());
    }

    public remote function hello(string req, grpc:Headers? headers = ()) returns ([string, grpc:Headers]|grpc:Error) {
        var unionResp = check self.grpcClient->blockingExecute("grpcservices.HelloWorld100/hello", req, headers);
        any result = ();
        grpc:Headers resHeaders = new;
        [result, resHeaders] = unionResp;
        return [result.toString(), resHeaders];
    }

    public remote function testInt(int req, grpc:Headers? headers = ()) returns ([int, grpc:Headers]|grpc:Error) {
        var unionResp = check self.grpcClient->blockingExecute("grpcservices.HelloWorld100/testInt", req, headers);
        anydata result = ();
        grpc:Headers resHeaders = new;
        [result, resHeaders] = unionResp;
        var value = result.cloneWithType(typedesc<int>);
        if (value is int) {
            return [value, resHeaders];
        } else {
            return grpc:InternalError("Error while constructing the message", value);
        }
    }

    public remote function testFloat(float req, grpc:Headers? headers = ()) returns ([float, grpc:Headers]|grpc:Error) {
        var unionResp = check self.grpcClient->blockingExecute("grpcservices.HelloWorld100/testFloat", req, headers);
        anydata result = ();
        grpc:Headers resHeaders = new;
        [result, resHeaders] = unionResp;
        var value = result.cloneWithType(typedesc<float>);
        if (value is float) {
            return [value, resHeaders];
        } else {
            return grpc:InternalError("Error while constructing the message", value);
        }
    }

    public remote function testBoolean(boolean req, grpc:Headers? headers = ()) returns ([boolean, grpc:Headers]|grpc:Error) {
        var unionResp = check self.grpcClient->blockingExecute("grpcservices.HelloWorld100/testBoolean", req, headers);
        anydata result = ();
        grpc:Headers resHeaders = new;
        [result, resHeaders] = unionResp;
        var value = result.cloneWithType(typedesc<boolean>);
        if (value is boolean) {
            return [value, resHeaders];
        } else {
            return grpc:InternalError("Error while constructing the message", value);
        }
    }

    public remote function testStruct(Request req, grpc:Headers? headers = ()) returns ([Response, grpc:Headers]|grpc:Error) {
        var unionResp = check self.grpcClient->blockingExecute("grpcservices.HelloWorld100/testStruct", req, headers);
        anydata result = ();
        grpc:Headers resHeaders = new;
        [result, resHeaders] = unionResp;
        var value = result.cloneWithType(typedesc<Response>);
        if (value is Response) {
            return [value, resHeaders];
        } else {
            return grpc:InternalError("Error while constructing the message", value);
        }
    }
};

public type HelloWorldClient client object {

    *grpc:AbstractClientEndpoint;

    private grpc:Client grpcClient;

    public function __init(string url, grpc:ClientConfiguration? config = ()) {
        // initialize client endpoint.
        self.grpcClient = new(url, config);
        checkpanic self.grpcClient.initStub(self, "non-blocking", ROOT_DESCRIPTOR, getDescriptorMap());
    }

    public remote function hello(string req, service msgListener, grpc:Headers? headers = ()) returns (grpc:Error?) {
        return self.grpcClient->nonBlockingExecute("grpcservices.HelloWorld100/hello", req, msgListener, headers);
    }

    public remote function testInt(int req, service msgListener, grpc:Headers? headers = ()) returns (grpc:Error?) {
        return self.grpcClient->nonBlockingExecute("grpcservices.HelloWorld100/testInt", req, msgListener, headers);
    }

    public remote function testFloat(float req, service msgListener, grpc:Headers? headers = ()) returns (grpc:Error?) {
        return self.grpcClient->nonBlockingExecute("grpcservices.HelloWorld100/testFloat", req, msgListener, headers);
    }

    public remote function testBoolean(boolean req, service msgListener, grpc:Headers? headers = ()) returns (grpc:Error?) {
        return self.grpcClient->nonBlockingExecute("grpcservices.HelloWorld100/testBoolean", req, msgListener, headers);
    }

    public remote function testStruct(Request req, service msgListener, grpc:Headers? headers = ()) returns (grpc:Error?) {
        return self.grpcClient->nonBlockingExecute("grpcservices.HelloWorld100/testStruct", req, msgListener, headers);
    }
};
const string ROOT_DESCRIPTOR = "0A1348656C6C6F576F726C643130302E70726F746F120C6772706373657276696365731A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F1A1B676F6F676C652F70726F746F6275662F656D7074792E70726F746F22490A075265717565737412120A046E616D6518012001280952046E616D6512180A076D65737361676518022001280952076D65737361676512100A036167651803200128035203616765221E0A08526573706F6E736512120A047265737018012001280952047265737032B5030A0D48656C6C6F576F726C6431303012430A0568656C6C6F121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C756512430A0774657374496E74121B2E676F6F676C652E70726F746F6275662E496E74363456616C75651A1B2E676F6F676C652E70726F746F6275662E496E74363456616C756512450A0974657374466C6F6174121B2E676F6F676C652E70726F746F6275662E466C6F617456616C75651A1B2E676F6F676C652E70726F746F6275662E466C6F617456616C756512450A0B74657374426F6F6C65616E121A2E676F6F676C652E70726F746F6275662E426F6F6C56616C75651A1A2E676F6F676C652E70726F746F6275662E426F6F6C56616C7565123B0A0A7465737453747275637412152E6772706373657276696365732E526571756573741A162E6772706373657276696365732E526573706F6E7365124F0A1774657374526573706F6E7365496E736964654D61746368121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A162E6772706373657276696365732E526573706F6E7365620670726F746F33";

function getDescriptorMap() returns map<string> {
    return {
    "HelloWorld100.proto":"0A1348656C6C6F576F726C643130302E70726F746F120C6772706373657276696365731A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F1A1B676F6F676C652F70726F746F6275662F656D7074792E70726F746F22490A075265717565737412120A046E616D6518012001280952046E616D6512180A076D65737361676518022001280952076D65737361676512100A036167651803200128035203616765221E0A08526573706F6E736512120A047265737018012001280952047265737032B5030A0D48656C6C6F576F726C6431303012430A0568656C6C6F121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C756512430A0774657374496E74121B2E676F6F676C652E70726F746F6275662E496E74363456616C75651A1B2E676F6F676C652E70726F746F6275662E496E74363456616C756512450A0974657374466C6F6174121B2E676F6F676C652E70726F746F6275662E466C6F617456616C75651A1B2E676F6F676C652E70726F746F6275662E466C6F617456616C756512450A0B74657374426F6F6C65616E121A2E676F6F676C652E70726F746F6275662E426F6F6C56616C75651A1A2E676F6F676C652E70726F746F6275662E426F6F6C56616C7565123B0A0A7465737453747275637412152E6772706373657276696365732E526571756573741A162E6772706373657276696365732E526573706F6E7365124F0A1774657374526573706F6E7365496E736964654D61746368121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A162E6772706373657276696365732E526573706F6E7365620670726F746F33",
    "google/protobuf/wrappers.proto":"0A0E77726170706572732E70726F746F120F676F6F676C652E70726F746F62756622230A0B446F75626C6556616C756512140A0576616C7565180120012801520576616C756522220A0A466C6F617456616C756512140A0576616C7565180120012802520576616C756522220A0A496E74363456616C756512140A0576616C7565180120012803520576616C756522230A0B55496E74363456616C756512140A0576616C7565180120012804520576616C756522220A0A496E74333256616C756512140A0576616C7565180120012805520576616C756522230A0B55496E74333256616C756512140A0576616C756518012001280D520576616C756522210A09426F6F6C56616C756512140A0576616C7565180120012808520576616C756522230A0B537472696E6756616C756512140A0576616C7565180120012809520576616C756522220A0A427974657356616C756512140A0576616C756518012001280C520576616C756542570A13636F6D2E676F6F676C652E70726F746F627566420D577261707065727350726F746F50015A057479706573F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33",
    "google/protobuf/empty.proto":"0A0B656D7074792E70726F746F120F676F6F676C652E70726F746F62756622070A05456D70747942540A13636F6D2E676F6F676C652E70726F746F627566420A456D70747950726F746F50015A057479706573F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33"
    };
}

public type Request record {
    string name = "";
    string message = "";
    int age = 0;
};

public type Response record {
    string resp = "";
};
