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

endpoint HelloWorldBlockingClient HelloWorldBlockingEp {
    url:"http://localhost:9090"
};

public function main(string... args) {
    Person p = {name:"Danesh", address:{postalCode:10300, state:"Western", country:"Sri Lanka"}};
    string output = testInputNestedStruct(p);
    io:println("testInputNestedStruct output: " + output);

    Person|string result = testOutputNestedStruct("WSO2");
    if (result is Person) {
        io:println("testOutputNestedStruct output: " + result.name);
    } else {
        io:println(result);
    }
}

function testInputNestedStruct(Person p) returns string {
    (string, grpc:Headers)|error unionResp = HelloWorldBlockingEp->testInputNestedStruct(p);
    if (unionResp is error) {
        io:println("Error from Connector: " + unionResp.reason());
        return "Error from Connector: " + unionResp.reason();
    } else {
        string result;
        (result, _) = unionResp;
        return result;
    }
}

function testOutputNestedStruct(string name) returns Person|string {
    (Person, grpc:Headers)|error unionResp = HelloWorldBlockingEp->testOutputNestedStruct(name);
    if (unionResp is error) {
        return "Error from Connector: " + unionResp.reason();
    } else {
        Person result;
        (result, _) = unionResp;
        return result;
    }
}

public type HelloWorldBlockingStub object {

    public grpc:Client clientEndpoint = new;
    public grpc:Stub stub = new;

    function initStub(grpc:Client ep) {
        grpc:Stub navStub = new;
        error? result = navStub.initStub(ep, "blocking", DESCRIPTOR_KEY, descriptorMap);
        if (result is error) {
            panic result;
        } else {
            self.stub = navStub;
        }
    }

    function testInputNestedStruct(Person req, grpc:Headers? headers = ()) returns ((string, grpc:Headers)|error) {
        var payload = check self.stub.blockingExecute("foo.HelloWorld/testInputNestedStruct", req, headers = headers);
        any result = ();
        grpc:Headers resHeaders = new;
        (result, resHeaders) = payload;
        return (<string>result, resHeaders);
    }

    function testOutputNestedStruct(string req, grpc:Headers? headers = ()) returns ((Person, grpc:Headers)|error) {
        var payload = check self.stub.blockingExecute("foo.HelloWorld/testOutputNestedStruct", req, headers = headers);
        any result;
        grpc:Headers resHeaders;
        (result, resHeaders) = payload;
        return (check <Person>result, resHeaders);
    }
};

public type HelloWorldStub object {

    public grpc:Client clientEndpoint = new;
    public grpc:Stub stub = new;

    function initStub(grpc:Client ep) {
        grpc:Stub navStub = new;
        error? result = navStub.initStub(ep, "non-blocking", DESCRIPTOR_KEY, descriptorMap);
        if (result is error) {
            panic result;
        } else {
            self.stub = navStub;
        }
    }

    function testInputNestedStruct(Person req, typedesc listener, grpc:Headers? headers = ()) returns error? {
        return self.stub.nonBlockingExecute("foo.HelloWorld/testInputNestedStruct", req, listener, headers = headers);
    }

    function testOutputNestedStruct(string req, typedesc listener, grpc:Headers? headers = ()) returns error? {
        return self.stub.nonBlockingExecute("foo.HelloWorld/testOutputNestedStruct", req, listener, headers = headers);
    }
};


public type HelloWorldBlockingClient object {

    public grpc:Client client = new;
    public HelloWorldBlockingStub stub = new;

    public function init(grpc:ClientEndpointConfig config) {
        // initialize client endpoint.
        grpc:Client c = new;
        c.init(config);
        self.client = c;
        // initialize service stub.
        HelloWorldBlockingStub s = new;
        s.initStub(c);
        self.stub = s;
    }

    public function getCallerActions() returns (HelloWorldBlockingStub) {
        return self.stub;
    }
};

public type HelloWorldClient object {

    public grpc:Client client = new;
    public HelloWorldStub stub = new;

    public function init(grpc:ClientEndpointConfig config) {
        // initialize client endpoint.
        grpc:Client c = new;
        c.init(config);
        self.client = c;
        // initialize service stub.
        HelloWorldStub s = new;
        s.initStub(c);
        self.stub = s;
    }

    public function getCallerActions() returns (HelloWorldStub) {
        return self.stub;
    }
};


type Person record {
    string name = "";
    Address address = {};

};

type Address record {
    int postalCode = 0;
    string state = "";
    string country = "";

};


@final string DESCRIPTOR_KEY = "target/grpc/HelloWorld.proto";
map descriptorMap =
{
    "target/grpc/HelloWorld.proto":"0A1C7461726765742F677270632F48656C6C6F576F726C642E70726F746F1203666F6F1A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F22440A06506572736F6E12120A046E616D6518012001280952046E616D6512260A076164647265737318022001280B320C2E666F6F2E4164647265737352076164647265737322590A0741646472657373121E0A0A706F7374616C436F6465180120012803520A706F7374616C436F646512140A0573746174651802200128095205737461746512180A07636F756E7472791803200128095207636F756E7472793295010A0A48656C6C6F576F726C6412420A1574657374496E7075744E6573746564537472756374120B2E666F6F2E506572736F6E1A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C756512430A16746573744F75747075744E6573746564537472756374121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A0B2E666F6F2E506572736F6E620670726F746F33",

    "google/protobuf/wrappers.proto":"0A0E77726170706572732E70726F746F120F676F6F676C652E70726F746F62756622230A0B446F75626C6556616C756512140A0576616C7565180120012801520576616C756522220A0A466C6F617456616C756512140A0576616C7565180120012802520576616C756522220A0A496E74363456616C756512140A0576616C7565180120012803520576616C756522230A0B55496E74363456616C756512140A0576616C7565180120012804520576616C756522220A0A496E74333256616C756512140A0576616C7565180120012805520576616C756522230A0B55496E74333256616C756512140A0576616C756518012001280D520576616C756522210A09426F6F6C56616C756512140A0576616C7565180120012808520576616C756522230A0B537472696E6756616C756512140A0576616C7565180120012809520576616C756522220A0A427974657356616C756512140A0576616C756518012001280C520576616C756542570A13636F6D2E676F6F676C652E70726F746F627566420D577261707065727350726F746F50015A057479706573F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33"

};
