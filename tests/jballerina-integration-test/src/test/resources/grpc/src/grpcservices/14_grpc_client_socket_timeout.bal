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

@grpc:ServiceDescriptor {
    descriptor: ROOT_DESCRIPTOR_14,
    descMap: getDescriptorMap14()
}
service HelloWorld14 on new grpc:Listener(9104) {

    resource function hello (grpc:Caller caller, string name,
                             grpc:Headers headers) {
        string message = "Hello " + name;
        runtime:sleep(2000);
        // Sends response message with headers.
        grpc:Error? err = caller->send(message);
        if (err is grpc:Error) {
            io:println("Error from Connector: " + err.reason() + " - "
                    + <string> err.detail()["message"]);
        }

        // Sends `completed` notification to caller.
        checkpanic caller->complete();
    }
}

const string ROOT_DESCRIPTOR_14 = "0A1E677270635F756E6172795F626C6F636B696E675F636F6E742E70726F746F1A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F32530A0C48656C6C6F576F726C64313412430A0568656C6C6F121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C7565620670726F746F33";
function getDescriptorMap14() returns map<string> {
    return {
        "grpc_unary_blocking_cont.proto":
            "0A1E677270635F756E6172795F626C6F636B696E675F636F6E742E70726F746F1A1E676F6F676C652F70726F746F6275662F7772617070"
            + "6572732E70726F746F32530A0C48656C6C6F576F726C64313412430A0568656C6C6F121C2E676F6F676C652E70726F746F6275662E53"
            + "7472696E6756616C75651A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C7565620670726F746F33",
        "google/protobuf/wrappers.proto":
            "0A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F120F676"
            + "F6F676C652E70726F746F62756622230A0B446F75626C6556616C756512140A057661"
            + "6C7565180120012801520576616C756522220A0A466C6F617456616C756512140A057"
            + "6616C7565180120012802520576616C756522220A0A496E74363456616C756512140A"
            + "0576616C7565180120012803520576616C756522230A0B55496E74363456616C75651"
            + "2140A0576616C7565180120012804520576616C756522220A0A496E74333256616C75"
            + "6512140A0576616C7565180120012805520576616C756522230A0B55496E743332566"
            + "16C756512140A0576616C756518012001280D520576616C756522210A09426F6F6C56"
            + "616C756512140A0576616C7565180120012808520576616C756522230A0B537472696"
            + "E6756616C756512140A0576616C7565180120012809520576616C756522220A0A4279"
            + "74657356616C756512140A0576616C756518012001280C520576616C756542570A136"
            + "36F6D2E676F6F676C652E70726F746F627566420D577261707065727350726F746F50"
            + "015A057479706573F80101A20203475042AA021E476F6F676C652E50726F746F62756"
            + "62E57656C6C4B6E6F776E5479706573620670726F746F33"
    };
}
