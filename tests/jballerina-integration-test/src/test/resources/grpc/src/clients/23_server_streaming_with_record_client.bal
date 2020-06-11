// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
//    int resp1 = testServerStreamingWithRecord("WSO2");
//    io:println(resp1);
//}

int total = 0;
boolean eof = false;

public function testServerStreamingWithRecord(string name) returns int {
    helloWorldServerStreamingClient helloWorldEp = new("http://localhost:9113");
    HelloRequest newreq = {name: name};
    grpc:Error? result = helloWorldEp->lotsOfReplies(newreq,
                                                    HelloWorldServerStreamingMessageListener);
    if (result is grpc:Error) {
        io:println("Error from Connector: " + result.reason() + " - "
                                            + <string> result.detail()["message"]);
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

service HelloWorldServerStreamingMessageListener = service {

    // The `resource` registered to receive server messages
    function onMessage(HelloResponse message) {
        io:println("Response received from server: " + message.toString());
        total = total + 1;
    }

    // The `resource` registered to receive server error messages
    function onError(error err) {
        io:println("Error from Connector: " + err.reason() + " - "
        + <string>err.detail()["message"]);
    }

    // The `resource` registered to receive server completed messages.
    function onComplete() {
        io:println("Server Complete Sending Responses.");
        eof = true;
    }
};

public type helloWorldServerStreamingClient client object {

    *grpc:AbstractClientEndpoint;

    private grpc:Client grpcClient;

    public function init(string url, grpc:ClientConfiguration? config = ()) {
        // initialize client endpoint.
        self.grpcClient = new(url, config);
        checkpanic self.grpcClient.initStub(self, "non-blocking", ROOT_DESCRIPTOR_23, getDescriptorMap23());
    }

    public remote function lotsOfReplies(HelloRequest req, service msgListener, grpc:Headers? headers = ()) returns (grpc:Error?) {
        return self.grpcClient->nonBlockingExecute("helloWorldServerStreaming/lotsOfReplies", req, msgListener, headers);
    }

};

public type HelloRequest record {|
    string name = "";
|};

public type HelloResponse record {|
    string message = "";
|};

const string ROOT_DESCRIPTOR_23 = "0A1F68656C6C6F576F726C6453657276657253747265616D696E672E70726F746F22220A0C48656C6C6F5265717565737412120A046E616D6518012001280952046E616D6522290A0D48656C6C6F526573706F6E736512180A076D65737361676518012001280952076D657373616765324D0A1968656C6C6F576F726C6453657276657253747265616D696E6712300A0D6C6F74734F665265706C696573120D2E48656C6C6F526571756573741A0E2E48656C6C6F526573706F6E73653001620670726F746F33";
function getDescriptorMap23() returns map<string> {
    return {
        "helloWorldServerStreaming.proto":"0A1F68656C6C6F576F726C6453657276657253747265616D696E672E70726F746F22220A0C48656C6C6F5265717565737412120A046E616D6518012001280952046E616D6522290A0D48656C6C6F526573706F6E736512180A076D65737361676518012001280952076D657373616765324D0A1968656C6C6F576F726C6453657276657253747265616D696E6712300A0D6C6F74734F665265706C696573120D2E48656C6C6F526571756573741A0E2E48656C6C6F526573706F6E73653001620670726F746F33"

    };
}
