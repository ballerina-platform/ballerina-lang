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

HelloWorldBlockingClient HelloWorldBlockingEp = new("http://localhost:9090");

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


public client class HelloWorldBlockingClient {

    private grpc:Client grpcClient;

    function init(string url, grpc:ClientConfiguration? config = ()) {
        // initialize client endpoint.
        grpc:Client c = new(url, config = config);
        error? result = c.initStub("blocking", ROOT_DESCRIPTOR, getDescriptorMap());
        if (result is error) {
            panic result;
        } else {
            self.grpcClient = c;
        }
    }

    remote function testInputNestedStruct(Person req, grpc:Headers? headers = ()) returns ((string, grpc:Headers)|error) {
        var payload = check self.grpcClient->blockingExecute("foo.HelloWorld/testInputNestedStruct", req, headers = headers);
        any result = ();
        grpc:Headers resHeaders = new;
        (result, resHeaders) = payload;
        return (<string>result, resHeaders);
    }

    remote function testOutputNestedStruct(string req, grpc:Headers? headers = ()) returns ((Person, grpc:Headers)|error) {
        var payload = check self.grpcClient->blockingExecute("foo.HelloWorld/testOutputNestedStruct", req, headers = headers);
        any result;
        grpc:Headers resHeaders;
        (result, resHeaders) = payload;
        var value = Person.convert(result);
        if (value is Person) {
            return (value, resHeaders);
        } else {
            return value;
        }
    }
}

public client class HelloWorldClient {

    private grpc:Client grpcClient;

    function init(string url, grpc:ClientConfiguration? config = ()) {
        // initialize client endpoint.
        grpc:Client c = new(url, config = config);
        error? result = c.initStub("non-blocking", ROOT_DESCRIPTOR, getDescriptorMap());
        if (result is error) {
            panic result;
        } else {
            self.grpcClient = c;
        }
    }

    remote function testInputNestedStruct(Person req, service msgListener, grpc:Headers? headers = ()) returns error? {
        return self.grpcClient->nonBlockingExecute("foo.HelloWorld/testInputNestedStruct", req, msgListener, headers = headers);
    }

    remote function testOutputNestedStruct(string req, service msgListener, grpc:Headers? headers = ()) returns
                                                                                                             error? {
        return self.grpcClient->nonBlockingExecute("foo.HelloWorld/testOutputNestedStruct", req, msgListener, headers = headers);
    }
}


type Person record {
    string name = "";
    Address address = {};

};

type Address record {
    int postalCode = 0;
    string state = "";
    string country = "";

};


const string ROOT_DESCRIPTOR = "0A1C7461726765742F677270632F48656C6C6F576F726C642E70726F746F1203666F6F1A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F22440A06506572736F6E12120A046E616D6518012001280952046E616D6512260A076164647265737318022001280B320C2E666F6F2E4164647265737352076164647265737322590A0741646472657373121E0A0A706F7374616C436F6465180120012803520A706F7374616C436F646512140A0573746174651802200128095205737461746512180A07636F756E7472791803200128095207636F756E7472793295010A0A48656C6C6F576F726C6412420A1574657374496E7075744E6573746564537472756374120B2E666F6F2E506572736F6E1A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C756512430A16746573744F75747075744E6573746564537472756374121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A0B2E666F6F2E506572736F6E620670726F746F33";
function getDescriptorMap() returns map<string> {
    return {
        "target/grpc/HelloWorld.proto":
        "0A1C7461726765742F677270632F48656C6C6F576F726C642E70726F746F1203666F6F1A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F22440A06506572736F6E12120A046E616D6518012001280952046E616D6512260A076164647265737318022001280B320C2E666F6F2E4164647265737352076164647265737322590A0741646472657373121E0A0A706F7374616C436F6465180120012803520A706F7374616C436F646512140A0573746174651802200128095205737461746512180A07636F756E7472791803200128095207636F756E7472793295010A0A48656C6C6F576F726C6412420A1574657374496E7075744E6573746564537472756374120B2E666F6F2E506572736F6E1A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C756512430A16746573744F75747075744E6573746564537472756374121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A0B2E666F6F2E506572736F6E620670726F746F33"
        ,

        "google/protobuf/wrappers.proto":
        "0A0E77726170706572732E70726F746F120F676F6F676C652E70726F746F62756622230A0B446F75626C6556616C756512140A0576616C7565180120012801520576616C756522220A0A466C6F617456616C756512140A0576616C7565180120012802520576616C756522220A0A496E74363456616C756512140A0576616C7565180120012803520576616C756522230A0B55496E74363456616C756512140A0576616C7565180120012804520576616C756522220A0A496E74333256616C756512140A0576616C7565180120012805520576616C756522230A0B55496E74333256616C756512140A0576616C756518012001280D520576616C756522210A09426F6F6C56616C756512140A0576616C7565180120012808520576616C756522230A0B537472696E6756616C756512140A0576616C7565180120012809520576616C756522220A0A427974657356616C756512140A0576616C756518012001280C520576616C756542570A13636F6D2E676F6F676C652E70726F746F627566420D577261707065727350726F746F50015A057479706573F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33"

    };
}
