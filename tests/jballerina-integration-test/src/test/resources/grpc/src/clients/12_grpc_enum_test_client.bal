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

// Enable when you need to test locally.
//public function main() {
//    string resp = testEnum();
//    io:println(resp);
//}

function testEnum() returns (string) {
    testEnumServiceBlockingClient blockingEp = new ("http://localhost:9102");

    orderInfo orderReq = { id:"100500", mode:r };
    var addResponse = blockingEp->testEnum(orderReq);
    if (addResponse is grpc:Error) {
        return io:sprintf("Error from Connector: %s - %s", addResponse.reason(), <string> addResponse.detail()["message"]);
    } else {
        string result = "";
        [result, _] = addResponse;
        return result;
    }
}

public type testEnumServiceBlockingClient client object {

    *grpc:AbstractClientEndpoint;

    private grpc:Client grpcClient;

    public function __init(string url, grpc:ClientConfiguration? config = ()) {
        // initialize client endpoint.
        self.grpcClient = new(url, config);
        checkpanic self.grpcClient.initStub(self, "blocking", ROOT_DESCRIPTOR, getDescriptorMap());
    }

    public remote function testEnum (orderInfo req, grpc:Headers? headers = ()) returns ([string, grpc:Headers]|grpc:Error) {
        var unionResp = check self.grpcClient->blockingExecute("grpcservices.testEnumService/testEnum", req, headers);
        grpc:Headers resHeaders = new;
        anydata result = ();
        [result, resHeaders] = unionResp;
        return [result.toString(), resHeaders];
    }
};

public type testEnumServiceClient client object {

    *grpc:AbstractClientEndpoint;

    private grpc:Client grpcClient;

    public function __init(string url, grpc:ClientConfiguration? config = ()) {
        // initialize client endpoint.
        self.grpcClient = new(url, config);
        checkpanic self.grpcClient.initStub(self, "non-blocking", ROOT_DESCRIPTOR, getDescriptorMap());
    }

    public remote function testEnum (orderInfo req, service msgListener, grpc:Headers? headers = ()) returns (grpc:Error?) {
        return self.grpcClient->nonBlockingExecute("grpcservices.testEnumService/testEnum", req, msgListener, headers);
    }
};


public type orderInfo record {
    string id = "";
    Mode mode = r;

};

public type Mode r;

public const r = "r";

const string ROOT_DESCRIPTOR = "0A1574657374456E756D536572766963652E70726F746F120C6772706373657276696365731A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F22430A096F72646572496E666F120E0A0269641801200128095202696412260A046D6F646518022001280E32122E6772706373657276696365732E4D6F646552046D6F64652A0D0A044D6F646512050A0172100032540A0F74657374456E756D5365727669636512410A0874657374456E756D12172E6772706373657276696365732E6F72646572496E666F1A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C7565620670726F746F33";
function getDescriptorMap() returns map<string> {
    return {
        "testEnumService.proto":
        "0A1574657374456E756D536572766963652E70726F746F120C6772706373657276696365731A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F22430A096F72646572496E666F120E0A0269641801200128095202696412260A046D6F646518022001280E32122E6772706373657276696365732E4D6F646552046D6F64652A0D0A044D6F646512050A0172100032540A0F74657374456E756D5365727669636512410A0874657374456E756D12172E6772706373657276696365732E6F72646572496E666F1A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C7565620670726F746F33"
        ,
        "google/protobuf/wrappers.proto":
        "0A0E77726170706572732E70726F746F120F676F6F676C652E70726F746F62756622230A0B446F75626C6556616C756512140A0576616C7565180120012801520576616C756522220A0A466C6F617456616C756512140A0576616C7565180120012802520576616C756522220A0A496E74363456616C756512140A0576616C7565180120012803520576616C756522230A0B55496E74363456616C756512140A0576616C7565180120012804520576616C756522220A0A496E74333256616C756512140A0576616C7565180120012805520576616C756522230A0B55496E74333256616C756512140A0576616C756518012001280D520576616C756522210A09426F6F6C56616C756512140A0576616C7565180120012808520576616C756522230A0B537472696E6756616C756512140A0576616C7565180120012809520576616C756522220A0A427974657356616C756512140A0576616C756518012001280C520576616C756542570A13636F6D2E676F6F676C652E70726F746F627566420D577261707065727350726F746F50015A057479706573F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33"
    };
}
