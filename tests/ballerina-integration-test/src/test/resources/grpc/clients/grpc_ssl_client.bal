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

function testUnarySecuredBlockingWithCerts(string path) returns (string) {
    endpoint grpcMutualSslServiceBlockingClient helloWorldBlockingEp {
        url:"https://localhost:9317",
        secureSocket:{
            keyFile: path + "/private.key",
            certFile: path + "/public.crt",
            trustedCertFile: path + "/public.crt"
        }
    };

    (string, grpc:Headers)|error unionResp = helloWorldBlockingEp->hello("WSO2");
    if (unionResp is error) {
        io:println("Error from Connector: " + unionResp.reason());
        return "Error from Connector: " + unionResp.reason();
    } else {
        string result;
        (result, _) = unionResp;
        io:println("Client Got Response : ");
        io:println(result);
        return result;
    }
}
// This is an auto generated client stub which is used to communicate between gRPC client.
public type grpcMutualSslServiceBlockingStub object {
    public grpc:Client clientEndpoint = new;
    public grpc:Stub stub = new;

    function initStub (grpc:Client ep) {
        grpc:Stub navStub = new;
        error? result = navStub.initStub(ep, "blocking", DESCRIPTOR_KEY, descriptorMap);
        if (result is error) {
            panic result;
        } else {
            self.stub = navStub;
        }
    }

    function hello (string req, grpc:Headers? headers = ()) returns ((string, grpc:Headers)|error) {

        var unionResp = check self.stub.blockingExecute("grpcservices.grpcMutualSslService/hello", req, headers = headers);
        grpc:Headers resHeaders = new;
        any result = ();
        (result, resHeaders) = unionResp;
        return (<string>result, resHeaders);
    }

};

public type grpcMutualSslServiceStub object {
    public grpc:Client clientEndpoint = new;
    public grpc:Stub stub = new;

    function initStub (grpc:Client ep) {
        grpc:Stub navStub = new;
        error? result = navStub.initStub(ep, "non-blocking", DESCRIPTOR_KEY, descriptorMap);
        if (result is error) {
            panic result;
        } else {
            self.stub = navStub;
        }
    }

    function hello (string req, typedesc listener, grpc:Headers? headers = ()) returns (error?) {

        return self.stub.nonBlockingExecute("grpcservices.grpcMutualSslService/hello", req, listener, headers = headers);
    }

};

public type grpcMutualSslServiceBlockingClient object {
    public grpc:Client client = new;
    public grpcMutualSslServiceBlockingStub stub = new;

    public function init (grpc:ClientEndpointConfig config) {
        // initialize client endpoint.
        grpc:Client c = new;
        c.init(config);
        self.client = c;
        // initialize service stub.
        grpcMutualSslServiceBlockingStub s = new;
        s.initStub(c);
        self.stub = s;
    }

    public function getCallerActions () returns grpcMutualSslServiceBlockingStub {
        return self.stub;
    }
};

public type grpcMutualSslServiceClient object {
    public grpc:Client client = new;
    public grpcMutualSslServiceStub stub = new;

    public function init (grpc:ClientEndpointConfig config) {
        // initialize client endpoint.
        grpc:Client c = new;
        c.init(config);
        self.client = c;
        // initialize service stub.
        grpcMutualSslServiceStub s = new;
        s.initStub(c);
        self.stub = s;
    }

    public function getCallerActions () returns grpcMutualSslServiceStub {
        return self.stub;
    }
};

@final string DESCRIPTOR_KEY = "grpcMutualSslService.proto";
map descriptorMap = {
    "grpcMutualSslService.proto":"0A1A677270634D757475616C53736C536572766963652E70726F746F120C6772706373657276696365731A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F325B0A14677270634D757475616C53736C5365727669636512430A0568656C6C6F121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C7565620670726F746F33",
    "google/protobuf/wrappers.proto":"0A0E77726170706572732E70726F746F120F676F6F676C652E70726F746F62756622230A0B446F75626C6556616C756512140A0576616C7565180120012801520576616C756522220A0A466C6F617456616C756512140A0576616C7565180120012802520576616C756522220A0A496E74363456616C756512140A0576616C7565180120012803520576616C756522230A0B55496E74363456616C756512140A0576616C7565180120012804520576616C756522220A0A496E74333256616C756512140A0576616C7565180120012805520576616C756522230A0B55496E74333256616C756512140A0576616C756518012001280D520576616C756522210A09426F6F6C56616C756512140A0576616C7565180120012808520576616C756522230A0B537472696E6756616C756512140A0576616C7565180120012809520576616C756522220A0A427974657356616C756512140A0576616C756518012001280C520576616C756542570A13636F6D2E676F6F676C652E70726F746F627566420D577261707065727350726F746F50015A057479706573F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33"

};
