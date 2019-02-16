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
// This is server implementation for bidirectional streaming scenario
import ballerina/grpc;
import ballerina/io;

// Server endpoint configuration
listener grpc:Listener ep3 = new (9095);

@grpc:ServiceConfig {name:"chat",
    clientStreaming:true,
    serverStreaming:true}
@grpc:ServiceDescriptor {
    descriptor: ROOT_DESCRIPTOR_3,
    descMap: getDescriptorMap3()
}
service Chat on ep3 {
    map<grpc:Caller> consMap = {};
    resource function onOpen(grpc:Caller caller) {
        io:println(string `{{caller.getId()}} connected to chat`);
        self.consMap[<string>caller.getId()] = caller;
    }

    resource function onMessage(grpc:Caller caller, ChatMessage chatMsg) {
        grpc:Caller conn;
        string msg = string `{{chatMsg.name}}: {{chatMsg.message}}`;
        io:println("Server received message: " + msg);
        foreach var con in self.consMap {
            string callerId;
            (callerId, conn) = con;
            error? err = conn->send(msg);
            if (err is error) {
                io:println("Error from Connector: " + err.reason() + " - "
                        + <string>err.detail().message);
            } else {
                io:println("Server message to caller " + callerId + " sent successfully.");
            }
        }
    }

    resource function onError(grpc:Caller caller, error err) {
        io:println("Error from Connector: " + err.reason() + " - "
                + <string>err.detail().message);
    }

    resource function onComplete(grpc:Caller caller) {
        grpc:Caller conn;
        string msg = string `{{caller.getId()}} left the chat`;
        io:println(msg);
        var v = self.consMap.remove(<string>caller.getId());
        foreach var con in self.consMap {
            string callerId;
            (callerId, conn) = con;
            error? err = conn->send(msg);
            if (err is error) {
                io:println("Error from Connector: " + err.reason() + " - "
                        + <string>err.detail().message);
            } else {
                io:println("Server message to caller " + callerId + " sent successfully.");
            }
        }
    }
}

type ChatMessage record {
    string name = "";
    string message = "";
};

const string ROOT_DESCRIPTOR_3 = "0A0A436861742E70726F746F1A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F22280A0B436861744D657373616765120A0A046E616D6518012809120D0A076D65737361676518022809323C0A044368617412340A0463686174120B436861744D6573736167651A1B676F6F676C652E70726F746F6275662E537472696E6756616C756528013001620670726F746F33";
function getDescriptorMap3() returns map<string> {
    return {
        "Chat.proto":
        "0A0A436861742E70726F746F1A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F22280A0B436861744D657373616765120A0A046E616D6518012809120D0A076D65737361676518022809323C0A044368617412340A0463686174120B436861744D6573736167651A1B676F6F676C652E70726F746F6275662E537472696E6756616C756528013001620670726F746F33"
        ,

        "google/protobuf/wrappers.proto":
        "0A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F120F676F6F676C652E70726F746F627566221C0A0B446F75626C6556616C7565120D0A0576616C7565180120012801221B0A0A466C6F617456616C7565120D0A0576616C7565180120012802221B0A0A496E74363456616C7565120D0A0576616C7565180120012803221C0A0B55496E74363456616C7565120D0A0576616C7565180120012804221B0A0A496E74333256616C7565120D0A0576616C7565180120012805221C0A0B55496E74333256616C7565120D0A0576616C756518012001280D221A0A09426F6F6C56616C7565120D0A0576616C7565180120012808221C0A0B537472696E6756616C7565120D0A0576616C7565180120012809221B0A0A427974657356616C7565120D0A0576616C756518012001280C427C0A13636F6D2E676F6F676C652E70726F746F627566420D577261707065727350726F746F50015A2A6769746875622E636F6D2F676F6C616E672F70726F746F6275662F7074797065732F7772617070657273F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33"

    };
}
