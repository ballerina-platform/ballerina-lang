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

// Enable when you need to test locally.
//public function main() {
//    string response = testByteArray();
//    io:println(response);
//}

function testByteArray() returns (string) {
    byteServiceBlockingClient blockingEp  = new ("http://localhost:9101");
    string statement = "Lion in Town.";
    byte[] bytes = statement.toBytes();
    var addResponse = blockingEp->checkBytes(bytes);
    if (addResponse is grpc:Error) {
        return io:sprintf("Error from Connector: %s", addResponse.message());
    } else {
        byte[] result = [];
        grpc:Headers resHeaders = new;
        [result, resHeaders] = addResponse;
        return "byte array works";
    }
}

function testLargeByteArray(string filePath) returns (string) {
    byteServiceBlockingClient blockingEp  = new ("http://localhost:9101");
    var rch = <@untainted> io:openReadableFile(filePath);
    if (rch is error) {
        return "Error while reading the file.";
    } else {
        var resultBytes = rch.read(10000);
        if (resultBytes is byte[]) {
            var addResponse = blockingEp->checkBytes(resultBytes);
            if (addResponse is grpc:Error) {
                return io:sprintf("Error from Connector: %s", addResponse.message());
            } else {
                byte[] result = [];
                [result, _] = addResponse;
                if(result == resultBytes) {
                    return "30KB file content transmitted successfully";
                } else {
                    return "Error while transmitting file content";
                }
            }
        } else {
            error err = resultBytes;
            return io:sprintf("File read error: %s", err.message());
        }

    }
}

public type byteServiceBlockingClient client object {

    *grpc:AbstractClientEndpoint;

    private grpc:Client grpcClient;

    public function __init(string url, grpc:ClientConfiguration? config = ()) {
        // initialize client endpoint.
        self.grpcClient = new(url, config);
        checkpanic self.grpcClient.initStub(self, "blocking", ROOT_DESCRIPTOR, getDescriptorMap());
    }

    public remote function checkBytes (byte[] req, grpc:Headers? headers = ()) returns ([byte[], grpc:Headers]|grpc:Error) {
        var unionResp = check self.grpcClient->blockingExecute("grpcservices.byteService/checkBytes", req, headers);
        grpc:Headers resHeaders = new;
        anydata result = ();
        [result, resHeaders] = unionResp;
        var value = result.cloneWithType(typedesc<byte[]>);
        if (value is byte[]) {
            return [value, resHeaders];
        } else {
            return grpc:InternalError("Error while constructing the message", value);
        }
    }
};

public type byteServiceClient client object {

    *grpc:AbstractClientEndpoint;

    private grpc:Client grpcClient;

    public function __init(string url, grpc:ClientConfiguration? config = ()) {
        // initialize client endpoint.
        self.grpcClient = new(url, config);
        checkpanic self.grpcClient.initStub(self, "non-blocking", ROOT_DESCRIPTOR, getDescriptorMap());
    }

    public remote function checkBytes (byte[] req, service msgListener, grpc:Headers? headers = ()) returns (grpc:Error?) {
        return self.grpcClient->nonBlockingExecute("grpcservices.byteService/checkBytes", req, msgListener, headers);
    }
};

const string ROOT_DESCRIPTOR = "0A1162797465536572766963652E70726F746F120C6772706373657276696365731A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F32550A0B627974655365727669636512460A0A636865636B4279746573121B2E676F6F676C652E70726F746F6275662E427974657356616C75651A1B2E676F6F676C652E70726F746F6275662E427974657356616C7565620670726F746F33";
function getDescriptorMap() returns map<string> {
    return {
        "byteService.proto":
        "0A1162797465536572766963652E70726F746F120C6772706373657276696365731A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F32550A0B627974655365727669636512460A0A636865636B4279746573121B2E676F6F676C652E70726F746F6275662E427974657356616C75651A1B2E676F6F676C652E70726F746F6275662E427974657356616C7565620670726F746F33"
        ,
        "google/protobuf/wrappers.proto":
        "0A0E77726170706572732E70726F746F120F676F6F676C652E70726F746F62756622230A0B446F75626C6556616C756512140A0576616C7565180120012801520576616C756522220A0A466C6F617456616C756512140A0576616C7565180120012802520576616C756522220A0A496E74363456616C756512140A0576616C7565180120012803520576616C756522230A0B55496E74363456616C756512140A0576616C7565180120012804520576616C756522220A0A496E74333256616C756512140A0576616C7565180120012805520576616C756522230A0B55496E74333256616C756512140A0576616C756518012001280D520576616C756522210A09426F6F6C56616C756512140A0576616C7565180120012808520576616C756522230A0B537472696E6756616C756512140A0576616C7565180120012809520576616C756522220A0A427974657356616C756512140A0576616C756518012001280C520576616C756542570A13636F6D2E676F6F676C652E70726F746F627566420D577261707065727350726F746F50015A057479706573F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33"
    };
}
