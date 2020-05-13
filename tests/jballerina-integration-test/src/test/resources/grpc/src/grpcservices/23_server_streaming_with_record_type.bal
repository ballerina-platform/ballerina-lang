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
import ballerina/log;

listener grpc:Listener helloWorldStreamingep = new (9113);

@grpc:ServiceDescriptor {
    descriptor: ROOT_DESCRIPTOR_23,
    descMap: getDescriptorMap23()
}
service helloWorldServerStreaming on helloWorldStreamingep {

    @grpc:ResourceConfig { streaming: true }
    resource function lotsOfReplies(grpc:Caller caller, HelloRequest value) {
        log:printInfo("Server received hello from " + value.name);
        string[] greets = ["Hi", "Hey", "GM"];

        foreach string greet in greets {
            string message = greet + " " + value.name;
            HelloResponse msg = {message: message};
            grpc:Error? err = caller->send(msg);
            if (err is grpc:Error) {
                log:printError("Error from Connector: " + err.reason() + " - "
                                           + <string> err.detail()["message"]);
            } else {
                log:printInfo("Send reply: " + msg.toString());
            }
        }

        grpc:Error? result = caller->complete();
        if (result is grpc:Error) {
            log:printError("Error in sending completed notification to caller",
                err = result);
        }
    }
}

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
