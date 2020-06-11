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
//    string resp = testUnarySecuredBlocking();
//    io:println(resp);
//}

function testUnarySecuredBlocking(string keystorePath, string truststorePath) returns (string) {
    HelloWorldBlockingClient helloWorldBlockingEp = new ("https://localhost:9099", {
        secureSocket:{
            trustStore:{
               path: truststorePath,
               password: "ballerina"
            },
            keyStore: {
                path: keystorePath,
                password: "ballerina"
            },
            protocol: {
                name: "TLSv1.2",
                versions: ["TLSv1.2","TLSv1.1"]
            },
            certValidation : {
                enable: false
            },
            ocspStapling : false
        }
    });

    [string, grpc:Headers]|grpc:Error unionResp = helloWorldBlockingEp->hello("WSO2");
    if (unionResp is grpc:Error) {
        return io:sprintf("Error from Connector: %s", unionResp.message());
    } else {
        string result;
        [result, _] = unionResp;
        io:println("Client Got Response : ");
        io:println(result);
        return result;
    }
}

public type HelloWorldBlockingClient client object {

    *grpc:AbstractClientEndpoint;

    private grpc:Client grpcClient;

    public function __init(string url, grpc:ClientConfiguration? config = ()) {
        // initialize client endpoint.
        self.grpcClient = new(url, config);
        checkpanic self.grpcClient.initStub(self, "blocking", ROOT_DESCRIPTOR, getDescriptorMap());
    }

    public remote function hello(string req, grpc:Headers? headers = ()) returns ([string, grpc:Headers]|grpc:Error) {
        var unionResp = check self.grpcClient->blockingExecute("grpcservices.HelloWorld85/hello", req, headers);
        any result = ();
        grpc:Headers resHeaders = new;
        [result, resHeaders] = unionResp;
        return [result.toString(), resHeaders];
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
        return self.grpcClient->nonBlockingExecute("grpcservices.HelloWorld85/hello", req, msgListener, headers);
    }
};


const string ROOT_DESCRIPTOR = "0A1248656C6C6F576F726C6438352E70726F746F120C6772706373657276696365731A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F32530A0C48656C6C6F576F726C64383512430A0568656C6C6F121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C7565620670726F746F33";
function getDescriptorMap() returns map<string> {
    return
    {
        "HelloWorld85.proto":
        "0A1248656C6C6F576F726C6438352E70726F746F120C6772706373657276696365731A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F32530A0C48656C6C6F576F726C64383512430A0568656C6C6F121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C7565620670726F746F33"
        ,

        "google/protobuf/wrappers.proto":
        "0A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F120F676F6F676C652E70726F746F627566221C0A0B446F75626C6556616C7565120D0A0576616C7565180120012801221B0A0A466C6F617456616C7565120D0A0576616C7565180120012802221B0A0A496E74363456616C7565120D0A0576616C7565180120012803221C0A0B55496E74363456616C7565120D0A0576616C7565180120012804221B0A0A496E74333256616C7565120D0A0576616C7565180120012805221C0A0B55496E74333256616C7565120D0A0576616C756518012001280D221A0A09426F6F6C56616C7565120D0A0576616C7565180120012808221C0A0B537472696E6756616C7565120D0A0576616C7565180120012809221B0A0A427974657356616C7565120D0A0576616C756518012001280C427C0A13636F6D2E676F6F676C652E70726F746F627566420D577261707065727350726F746F50015A2A6769746875622E636F6D2F676F6C616E672F70726F746F6275662F7074797065732F7772617070657273F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33"

    };
}
