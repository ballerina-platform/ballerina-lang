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

// Enable when you need to test locally.
//public function main() {
//    string resp = testClientStreaming(["A", "B", "C"]);
//    io:println(resp);
//}

string response = "";
int total = 0;
function testClientStreaming(string[] args) returns (string) {
    // Client endpoint configuration
    HelloWorldClient helloWorldEp = new ("http://localhost:9094");

    grpc:StreamingClient ep = new;
    // Executing unary non-blocking call registering server message listener.
    var res = helloWorldEp->lotsOfGreetings(HelloWorldMessageListener);
    if (res is grpc:Error) {
        io:println("Error from Connector: " + res.reason() + " - " + <string> res.detail()["message"]);
    } else {
        ep = res;
    }
    io:println("Initialized connection sucessfully.");

    foreach var greet in args {
        grpc:Error? err = ep->send(greet);
        if (err is grpc:Error) {
            io:println("Error from Connector: " + err.reason() + " - " + <string> err.detail()["message"]);
        }
    }
    checkpanic ep->complete();

    int waitCount = 0;
    while(total < 1) {
        runtime:sleep(1000);
        io:println("msg count: ", total);
        if (waitCount > 10) {
            break;
        }
        waitCount += 1;
    }
    io:println("completed successfully");
    return response;
}

// Server Message Listener.
service HelloWorldMessageListener = service {

    // Resource registered to receive server messages
    function onMessage(string message) {
        response = <@untainted> message;
        io:println("Response received from server: " + response);
        total = 1;
    }

    // Resource registered to receive server error messages
    function onError(error err) {
        io:println("Error from Connector: " + err.reason() + " - " + <string> err.detail()["message"]);
    }

    // Resource registered to receive server completed message.
    function onComplete() {
        total = 1;
        io:println("Server Complete Sending Responses.");
    }
};

// Non-blocking client endpoint
public type HelloWorldClient client object {

    *grpc:AbstractClientEndpoint;

    private grpc:Client? grpcClient = ();

    public function __init(string url, grpc:ClientConfiguration? config = ()) {
        // initialize client endpoint.
        grpc:Client c = new(url, config);
        grpc:Error? result = c.initStub(self, "non-blocking", ROOT_DESCRIPTOR, getDescriptorMap());
        if (result is grpc:Error) {
            error err = result;
            panic err;
        } else {
            self.grpcClient = c;
        }
    }

    public remote function lotsOfGreetings(service msgListener, grpc:Headers? headers = ()) returns (grpc:StreamingClient|grpc:Error) {
        if !(self.grpcClient is grpc:Client) {
            error err = error("UninitializedFieldsErrorType", message = "Field(s) are not initialized");
            return grpc:prepareError(grpc:INTERNAL_ERROR, "Field(s) are not initialized", err);
        }
        grpc:Client tempGrpcClient = <grpc:Client> self.grpcClient;
        return tempGrpcClient->streamingExecute("grpcservices.HelloWorld7/lotsOfGreetings", msgListener, headers);
    }
};

const string ROOT_DESCRIPTOR = "0A1148656C6C6F576F726C64372E70726F746F120C6772706373657276696365731A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F325E0A0B48656C6C6F576F726C6437124F0A0F6C6F74734F664772656574696E6773121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C75652801620670726F746F33";
function getDescriptorMap() returns map<string> {
    return {
        "HelloWorld7.proto":
        "0A1148656C6C6F576F726C64372E70726F746F120C6772706373657276696365731A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F325E0A0B48656C6C6F576F726C6437124F0A0F6C6F74734F664772656574696E6773121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C75652801620670726F746F33"
        ,

        "google/protobuf/wrappers.proto":
        "0A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F120F676F6F676C652E70726F746F627566221C0A0B446F75626C6556616C7565120D0A0576616C7565180120012801221B0A0A466C6F617456616C7565120D0A0576616C7565180120012802221B0A0A496E74363456616C7565120D0A0576616C7565180120012803221C0A0B55496E74363456616C7565120D0A0576616C7565180120012804221B0A0A496E74333256616C7565120D0A0576616C7565180120012805221C0A0B55496E74333256616C7565120D0A0576616C756518012001280D221A0A09426F6F6C56616C7565120D0A0576616C7565180120012808221C0A0B537472696E6756616C7565120D0A0576616C7565180120012809221B0A0A427974657356616C7565120D0A0576616C756518012001280C427C0A13636F6D2E676F6F676C652E70726F746F627566420D577261707065727350726F746F50015A2A6769746875622E636F6D2F676F6C616E672F70726F746F6275662F7074797065732F7772617070657273F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33"

    };
}
