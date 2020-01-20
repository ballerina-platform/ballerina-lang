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
// This is client implementation for server streaming scenario
import ballerina/grpc;
import ballerina/io;
import ballerina/runtime;

// Enable when you need to test locally.
//public function main() {
//    int resp1 = testServerStreaming("WSO2");
//    io:println(resp1);
//}

int total = 0;
boolean eof = false;
function testServerStreaming(string name) returns int {
    // Client endpoint configuration
    HelloWorldClient helloWorldEp = new("http://localhost:9096");

    // Executing unary non-blocking call registering server message listener.
    grpc:Error? result = helloWorldEp->lotsOfReplies(name, HelloWorldMessageListener);
    if (result is grpc:Error) {
        io:println("Error from Connector: " + result.reason() + " - " + <string> result.detail()["message"]);
        return total;
    } else {
        io:println("Connected successfully");
    }

    int waitCount = 0;
    while(total < 3 || !eof) {
        runtime:sleep(1000);
        io:println("msg count: " + total.toString());
        if (waitCount > 10) {
            break;
        }
        waitCount += 1;
    }
    io:println("Client got response successfully.");
    io:println("responses count: " + total.toString());
    return total;
}

// Server Message Listener.
service HelloWorldMessageListener = service {

    // Resource registered to receive server messages
    resource function onMessage(string message) {
        lock {
            io:println("Response received from server: " + message);
            total = total + 1;
        }
    }

    // Resource registered to receive server error messages
    resource function onError(error err) {
        io:println("Error from Connector: " + err.reason() + " - " + <string> err.detail()["message"]);
    }

    // Resource registered to receive server completed message.
    resource function onComplete() {
        io:println("Server Complete Sending Response.");
        eof = true;
    }
};

// Non-blocking client endpoint
public type HelloWorldClient client object {

    *grpc:AbstractClientEndpoint;

    private grpc:Client grpcClient;

    public function __init(string url, grpc:ClientConfiguration? config = ()) {
        // initialize client endpoint.
        self.grpcClient = new(url, config);
        grpc:Error? result = self.grpcClient.initStub(self, "non-blocking", ROOT_DESCRIPTOR, getDescriptorMap());
    }

    public remote function lotsOfReplies(string req, service msgListener, grpc:Headers? headers = ()) returns (grpc:Error?) {
        return self.grpcClient->nonBlockingExecute("grpcservices.HelloWorld45/lotsOfReplies", req, msgListener, headers);
    }
};

const string ROOT_DESCRIPTOR = "0A1248656C6C6F576F726C6434352E70726F746F120C6772706373657276696365731A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F325D0A0C48656C6C6F576F726C643435124D0A0D6C6F74734F665265706C696573121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C75653001620670726F746F33";
function getDescriptorMap() returns map<string> {
    return {
        "HelloWorld45.proto":
        "0A1248656C6C6F576F726C6434352E70726F746F120C6772706373657276696365731A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F325D0A0C48656C6C6F576F726C643435124D0A0D6C6F74734F665265706C696573121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C75653001620670726F746F33"
        ,

        "google/protobuf/wrappers.proto":
        "0A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F120F676F6F676C652E70726F746F627566221C0A0B446F75626C6556616C7565120D0A0576616C7565180120012801221B0A0A466C6F617456616C7565120D0A0576616C7565180120012802221B0A0A496E74363456616C7565120D0A0576616C7565180120012803221C0A0B55496E74363456616C7565120D0A0576616C7565180120012804221B0A0A496E74333256616C7565120D0A0576616C7565180120012805221C0A0B55496E74333256616C7565120D0A0576616C756518012001280D221A0A09426F6F6C56616C7565120D0A0576616C7565180120012808221C0A0B537472696E6756616C7565120D0A0576616C7565180120012809221B0A0A427974657356616C7565120D0A0576616C756518012001280C427C0A13636F6D2E676F6F676C652E70726F746F627566420D577261707065727350726F746F50015A2A6769746875622E636F6D2F676F6C616E672F70726F746F6275662F7074797065732F7772617070657273F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33"

    };
}
