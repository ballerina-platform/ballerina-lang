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

function testByteArray() returns (string) {
    endpoint byteServiceBlockingClient blockingEp {
        url:"http://localhost:8557"
    };
    string statement = "Lion in Town.";
    byte[] bytes = statement.toByteArray("UTF-8");
    var addResponse = blockingEp->checkBytes(bytes);
    match addResponse {
        (byte[], grpc:Headers) payload => {
            byte[] result;
            grpc:Headers resHeaders;
            (result, resHeaders) = payload;
            return "byte array works";
        }
        error err => {
            return "Error from Connector: " + err.message;
        }
    }
}

// This is an auto generated code segment of the client stub.
public type byteServiceBlockingStub object {
    public grpc:Client clientEndpoint;
    public grpc:Stub stub;

    function initStub (grpc:Client ep) {
        grpc:Stub navStub = new;
        navStub.initStub(ep, "blocking", DESCRIPTOR_KEY, descriptorMap);
        self.stub = navStub;
    }

    function checkBytes (byte[] req, grpc:Headers? headers = ()) returns ((byte[], grpc:Headers)|error) {
        var unionResp = self.stub.blockingExecute("grpcservices.byteService/checkBytes", req, headers = headers);
        match unionResp {
            error payloadError => {
                return payloadError;
            }
            (any, grpc:Headers) payload => {
                grpc:Headers resHeaders;
                any result;
                (result, resHeaders) = payload;
                return (check <byte[]>result, resHeaders);
            }
        }
    }
};

public type byteServiceStub object {
    public grpc:Client clientEndpoint;
    public grpc:Stub stub;

    function initStub (grpc:Client ep) {
        grpc:Stub navStub = new;
        navStub.initStub(ep, "non-blocking", DESCRIPTOR_KEY, descriptorMap);
        self.stub = navStub;
    }

    function checkBytes (byte[] req, typedesc listener, grpc:Headers? headers = ()) returns (error?) {
        return self.stub.nonBlockingExecute("grpcservices.byteService/checkBytes", req, listener, headers = headers);
    }
};

public type byteServiceBlockingClient object {
    public grpc:Client client;
    public byteServiceBlockingStub stub;

    public function init (grpc:ClientEndpointConfig config) {
        // initialize client endpoint.
        grpc:Client c = new;
        c.init(config);
        self.client = c;
        // initialize service stub.
        byteServiceBlockingStub s = new;
        s.initStub(c);
        self.stub = s;
    }

    public function getCallerActions () returns byteServiceBlockingStub {
        return self.stub;
    }
};

public type byteServiceClient object {
    public grpc:Client client;
    public byteServiceStub stub;

    public function init (grpc:ClientEndpointConfig config) {
        // initialize client endpoint.
        grpc:Client c = new;
        c.init(config);
        self.client = c;
        // initialize service stub.
        byteServiceStub s = new;
        s.initStub(c);
        self.stub = s;
    }

    public function getCallerActions () returns byteServiceStub {
        return self.stub;
    }
};

@final string DESCRIPTOR_KEY = "grpcservices.byteService.proto";
map descriptorMap = {
    "grpcservices.byteService.proto":"0A1162797465536572766963652E70726F746F120C6772706373657276696365731A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F32550A0B627974655365727669636512460A0A636865636B4279746573121B2E676F6F676C652E70726F746F6275662E427974657356616C75651A1B2E676F6F676C652E70726F746F6275662E427974657356616C7565620670726F746F33",
    "google.protobuf.wrappers.proto":"0A0E77726170706572732E70726F746F120F676F6F676C652E70726F746F62756622230A0B446F75626C6556616C756512140A0576616C7565180120012801520576616C756522220A0A466C6F617456616C756512140A0576616C7565180120012802520576616C756522220A0A496E74363456616C756512140A0576616C7565180120012803520576616C756522230A0B55496E74363456616C756512140A0576616C7565180120012804520576616C756522220A0A496E74333256616C756512140A0576616C7565180120012805520576616C756522230A0B55496E74333256616C756512140A0576616C756518012001280D520576616C756522210A09426F6F6C56616C756512140A0576616C7565180120012808520576616C756522230A0B537472696E6756616C756512140A0576616C7565180120012809520576616C756522220A0A427974657356616C756512140A0576616C756518012001280C520576616C756542570A13636F6D2E676F6F676C652E70726F746F627566420D577261707065727350726F746F50015A057479706573F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33"
};
