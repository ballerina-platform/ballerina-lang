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

HelloWorldBlockingClient HelloWorldBlockingEp = new("http://localhost:9091");
const string ERROR_MSG_FORMAT = "Error from Connector: %s";

// Enable when you need to test locally.
//public function main() {
//    Person p = {
//        name : "Danesh"
//    };
//    string resp1 = testInputNestedStruct(p);
//    io:println(resp1);
//
//    var resp2 = testOutputNestedStruct("WSO2");
//    io:println(resp2);
//
//    var resp3 = testInputStructOutputStruct();
//    io:println(resp3);
//
//    var resp4 = testNoInputOutputStruct();
//    io:println(resp4);
//
//    var resp5 = testNoInputOutputArray();
//    io:println(resp5);
//
//    StockQuote sq = {
//        symbol: "IBM",
//        name: "IBM",
//        last: 0.0,
//        low: 0.0,
//        high: 0.0
//    };
//    var resp6 = testInputStructNoOutput(sq);
//    io:println(resp6);
//}

function testInputNestedStruct() returns (string) {
    Person p = {name:"Sam", address:{postalCode:10300, state:"Western", country:"Sri Lanka"}};
    io:println("testInputNestedStruct: input:");
    io:println(p);
    [string, grpc:Headers]|error unionResp = HelloWorldBlockingEp->testInputNestedStruct(p);
    io:println(unionResp);
    if (unionResp is error) {
        return io:sprintf(ERROR_MSG_FORMAT, unionResp.message());
    } else {
        io:println("Client Got Response : ");
        string result = "";
        [result, _] = unionResp;
        io:println(result);
        return "Client got response: " + result;
    }
}

function testOutputNestedStruct(string name) returns (Person|string) {
    io:println("testOutputNestedStruct: input: " + name);
    [Person, grpc:Headers]|grpc:Error unionResp = HelloWorldBlockingEp->testOutputNestedStruct(name);
    io:println(unionResp);
    if (unionResp is grpc:Error) {
        return io:sprintf(ERROR_MSG_FORMAT, unionResp.message());
    } else {
        io:println("Client Got Response : ");
        Person result = {};
        [result, _] = unionResp;
        io:println(result);
        return result;
    }
}

function testInputStructOutputStruct() returns (StockQuote|string) {
    StockRequest request = {name: "WSO2"};
    io:println("testInputStructOutputStruct: input:");
    io:println(request);
    [StockQuote, grpc:Headers]|grpc:Error unionResp = HelloWorldBlockingEp->testInputStructOutputStruct(request);
    io:println(unionResp);
    if (unionResp is grpc:Error) {
        return io:sprintf(ERROR_MSG_FORMAT, unionResp.message());
    } else {
        io:println("Client Got Response : ");
        StockQuote result = {};
        [result, _] = unionResp;
        io:println(result);
        return result;
    }
}

function testNoInputOutputStruct() returns (StockQuotes|string) {
    io:println("testNoInputOutputStruct: No input:");
    [StockQuotes, grpc:Headers]|grpc:Error unionResp = HelloWorldBlockingEp->testNoInputOutputStruct();
    io:println(unionResp);
    if (unionResp is grpc:Error) {
        return io:sprintf(ERROR_MSG_FORMAT, unionResp.message());
    } else {
        io:println("Client Got Response : ");
        StockQuotes result = {};
        [result, _] = unionResp;
        io:println(result);
        return result;
    }
}

function testNoInputOutputArray() returns (StockNames|string) {
    io:println("testNoInputOutputStruct: No input:");
    [StockNames, grpc:Headers]|grpc:Error unionResp = HelloWorldBlockingEp->testNoInputOutputArray();
    io:println(unionResp);
    if (unionResp is grpc:Error) {
        return io:sprintf(ERROR_MSG_FORMAT, unionResp.message());
    } else {
        io:println("Client Got Response : ");
        StockNames result = {};
        [result, _] = unionResp;
        io:println(result);
        return result;
    }
}

function testInputStructNoOutput(StockQuote quote) returns (string) {
    io:println("testNoInputOutputStruct: input:");
    io:println(quote);
    (grpc:Headers)|grpc:Error unionResp = HelloWorldBlockingEp->testInputStructNoOutput(quote);
    io:println(unionResp);
    if (unionResp is grpc:Error) {
        return io:sprintf(ERROR_MSG_FORMAT, unionResp.message());
    } else {
        io:println("Client Got No Response : ");
        _ = unionResp;
        io:println(unionResp);
        return "No Response";
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

    public remote function testInputNestedStruct(Person req, grpc:Headers? headers = ()) returns ([string, grpc:Headers]|grpc:Error) {
        [anydata, grpc:Headers] payload = check self.grpcClient->blockingExecute("grpcservices.HelloWorld/testInputNestedStruct", req, headers);
        anydata result = ();
        grpc:Headers resHeaders;
        io:println(payload);
        [result, resHeaders] = payload;
        return [result.toString(), resHeaders];
    }

    public remote function testOutputNestedStruct(string req, grpc:Headers? headers = ()) returns ([Person, grpc:Headers]|grpc:Error) {
        [anydata, grpc:Headers] payload = check self.grpcClient->blockingExecute("grpcservices.HelloWorld/testOutputNestedStruct", req, headers);
        anydata result = ();
        grpc:Headers resHeaders;
        [result, resHeaders] = payload;
        var value = result.cloneWithType(typedesc<Person>);
        if (value is Person) {
            return [value, resHeaders];
        } else {
            return grpc:InternalError("Error while constructing the message", value);
        }
    }

    public remote function testInputStructOutputStruct(StockRequest req, grpc:Headers? headers = ()) returns ([StockQuote, grpc:Headers]|grpc:Error) {
        [anydata, grpc:Headers] payload = check self.grpcClient->blockingExecute("grpcservices.HelloWorld/testInputStructOutputStruct", req, headers);
        anydata result = ();
        grpc:Headers resHeaders;
        [result, resHeaders] = payload;
        var value = result.cloneWithType(typedesc<StockQuote>);
        if (value is StockQuote) {
            return [value, resHeaders];
        } else {
            return grpc:InternalError("Error while constructing the message", value);
        }
    }

    public remote function testInputStructNoOutput(StockQuote req, grpc:Headers? headers = ()) returns ((grpc:Headers)|grpc:Error) {
        [anydata, grpc:Headers] payload = check self.grpcClient->blockingExecute("grpcservices.HelloWorld/testInputStructNoOutput", req, headers);
        anydata result = ();
        grpc:Headers resHeaders;
        [_, resHeaders] = payload;
        return resHeaders;
    }

    public remote function testNoInputOutputStruct(grpc:Headers? headers = ()) returns ([StockQuotes, grpc:Headers]|grpc:Error) {
        Empty req = {};
        [anydata, grpc:Headers] payload = check self.grpcClient->blockingExecute("grpcservices.HelloWorld/testNoInputOutputStruct", req, headers);
        anydata result = ();
        grpc:Headers resHeaders;
        [result, resHeaders] = payload;
        var value = result.cloneWithType(typedesc<StockQuotes>);
        if (value is StockQuotes) {
            return [value, resHeaders];
        } else {
            return grpc:InternalError("Error while constructing the message", value);
        }
    }

    public remote function testNoInputOutputArray(grpc:Headers? headers = ()) returns ([StockNames, grpc:Headers]|grpc:Error) {
        Empty req = {};
        [anydata, grpc:Headers] payload = check self.grpcClient->blockingExecute("grpcservices.HelloWorld/testNoInputOutputArray", req, headers);
        anydata result = ();
        grpc:Headers resHeaders;
        [result, resHeaders] = payload;
        var value = result.cloneWithType(typedesc<StockNames>);
        if (value is StockNames) {
            return [value, resHeaders];
        } else {
            return grpc:InternalError("Error while constructing the message", value);
        }
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

    public remote function testInputNestedStruct(Person req, service msgListener, grpc:Headers? headers = ()) returns (grpc:Error?) {
        return self.grpcClient->nonBlockingExecute("grpcservices.HelloWorld/testInputNestedStruct", req, msgListener,
         headers);
    }

    public remote function testOutputNestedStruct(string req, service msgListener, grpc:Headers? headers = ()) returns (grpc:Error?) {
        return self.grpcClient->nonBlockingExecute("grpcservices.HelloWorld/testOutputNestedStruct", req, msgListener,
            headers);
    }

    public remote function testInputStructOutputStruct(StockRequest req, service msgListener, grpc:Headers? headers = ()) returns (grpc:Error?) {
        return self.grpcClient->nonBlockingExecute("grpcservices.HelloWorld/testInputStructOutputStruct", req, msgListener,
            headers);
    }

    public remote function testInputStructNoOutput(StockQuote req, service msgListener, grpc:Headers? headers = ()) returns (grpc:Error?) {
        return self.grpcClient->nonBlockingExecute("grpcservices.HelloWorld/testInputStructNoOutput", req, msgListener,
            headers);
    }

    public remote function testNoInputOutputStruct(Empty req, service msgListener, grpc:Headers? headers = ()) returns (grpc:Error?) {
        return self.grpcClient->nonBlockingExecute("grpcservices.HelloWorld/testNoInputOutputStruct", req, msgListener,
            headers);
    }

    public remote function testNoInputOutputArray(Empty req, service msgListener, grpc:Headers? headers = ()) returns (grpc:Error?) {
        return self.grpcClient->nonBlockingExecute("grpcservices.HelloWorld/testNoInputOutputArray", req, msgListener,
            headers);
    }
};


public type Person record {
    string name = "";
    Address address = {};

};

public type Address record {
    int postalCode = 0;
    string state = "";
    string country = "";

};

public type StockRequest record {
    string name = "";

};

public type StockQuote record {
    string symbol = "";
    string name = "";
    float last = 0.0;
    float low = 0.0;
    float high = 0.0;

};

type StockQuotes record {
    StockQuote[] stock = [];
};

type StockNames record {
    string[] names = [];

};

public type Empty record {

};


const string ROOT_DESCRIPTOR = "0A1048656C6C6F576F726C642E70726F746F120C6772706373657276696365731A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F1A1B676F6F676C652F70726F746F6275662F656D7074792E70726F746F224D0A06506572736F6E12120A046E616D6518012001280952046E616D65122F0A076164647265737318022001280B32152E6772706373657276696365732E4164647265737352076164647265737322590A0741646472657373121E0A0A706F7374616C436F6465180120012803520A706F7374616C436F646512140A0573746174651802200128095205737461746512180A07636F756E7472791803200128095207636F756E74727922220A0C53746F636B5265717565737412120A046E616D6518012001280952046E616D6522720A0A53746F636B51756F746512160A0673796D626F6C180120012809520673796D626F6C12120A046E616D6518022001280952046E616D6512120A046C61737418032001280252046C61737412100A036C6F7718042001280252036C6F7712120A0468696768180520012802520468696768223D0A0B53746F636B51756F746573122E0A0573746F636B18012003280B32182E6772706373657276696365732E53746F636B51756F7465520573746F636B22220A0A53746F636B4E616D657312140A056E616D657318012003280952056E616D657332E3030A0A48656C6C6F576F726C64124B0A1574657374496E7075744E657374656453747275637412142E6772706373657276696365732E506572736F6E1A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C7565124C0A16746573744F75747075744E6573746564537472756374121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A142E6772706373657276696365732E506572736F6E12530A1B74657374496E7075745374727563744F7574707574537472756374121A2E6772706373657276696365732E53746F636B526571756573741A182E6772706373657276696365732E53746F636B51756F7465124B0A1774657374496E7075745374727563744E6F4F757470757412182E6772706373657276696365732E53746F636B51756F74651A162E676F6F676C652E70726F746F6275662E456D707479124C0A17746573744E6F496E7075744F757470757453747275637412162E676F6F676C652E70726F746F6275662E456D7074791A192E6772706373657276696365732E53746F636B51756F746573124A0A16746573744E6F496E7075744F7574707574417272617912162E676F6F676C652E70726F746F6275662E456D7074791A182E6772706373657276696365732E53746F636B4E616D6573620670726F746F33";
function getDescriptorMap() returns map<string> {
    return {
    "HelloWorld.proto":
    "0A1048656C6C6F576F726C642E70726F746F120C6772706373657276696365731A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F1A1B676F6F676C652F70726F746F6275662F656D7074792E70726F746F224D0A06506572736F6E12120A046E616D6518012001280952046E616D65122F0A076164647265737318022001280B32152E6772706373657276696365732E4164647265737352076164647265737322590A0741646472657373121E0A0A706F7374616C436F6465180120012803520A706F7374616C436F646512140A0573746174651802200128095205737461746512180A07636F756E7472791803200128095207636F756E74727922220A0C53746F636B5265717565737412120A046E616D6518012001280952046E616D6522720A0A53746F636B51756F746512160A0673796D626F6C180120012809520673796D626F6C12120A046E616D6518022001280952046E616D6512120A046C61737418032001280252046C61737412100A036C6F7718042001280252036C6F7712120A0468696768180520012802520468696768223D0A0B53746F636B51756F746573122E0A0573746F636B18012003280B32182E6772706373657276696365732E53746F636B51756F7465520573746F636B22220A0A53746F636B4E616D657312140A056E616D657318012003280952056E616D657332E3030A0A48656C6C6F576F726C64124B0A1574657374496E7075744E657374656453747275637412142E6772706373657276696365732E506572736F6E1A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C7565124C0A16746573744F75747075744E6573746564537472756374121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A142E6772706373657276696365732E506572736F6E12530A1B74657374496E7075745374727563744F7574707574537472756374121A2E6772706373657276696365732E53746F636B526571756573741A182E6772706373657276696365732E53746F636B51756F7465124B0A1774657374496E7075745374727563744E6F4F757470757412182E6772706373657276696365732E53746F636B51756F74651A162E676F6F676C652E70726F746F6275662E456D707479124C0A17746573744E6F496E7075744F757470757453747275637412162E676F6F676C652E70726F746F6275662E456D7074791A192E6772706373657276696365732E53746F636B51756F746573124A0A16746573744E6F496E7075744F7574707574417272617912162E676F6F676C652E70726F746F6275662E456D7074791A182E6772706373657276696365732E53746F636B4E616D6573620670726F746F33"
    ,"google/protobuf/wrappers.proto":
    "0A0E77726170706572732E70726F746F120F676F6F676C652E70726F746F62756622230A0B446F75626C6556616C756512140A0576616C7565180120012801520576616C756522220A0A466C6F617456616C756512140A0576616C7565180120012802520576616C756522220A0A496E74363456616C756512140A0576616C7565180120012803520576616C756522230A0B55496E74363456616C756512140A0576616C7565180120012804520576616C756522220A0A496E74333256616C756512140A0576616C7565180120012805520576616C756522230A0B55496E74333256616C756512140A0576616C756518012001280D520576616C756522210A09426F6F6C56616C756512140A0576616C7565180120012808520576616C756522230A0B537472696E6756616C756512140A0576616C7565180120012809520576616C756522220A0A427974657356616C756512140A0576616C756518012001280C520576616C756542570A13636F6D2E676F6F676C652E70726F746F627566420D577261707065727350726F746F50015A057479706573F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33"
    ,"google/protobuf/empty.proto":
    "0A0B656D7074792E70726F746F120F676F6F676C652E70726F746F62756622070A05456D70747942540A13636F6D2E676F6F676C652E70726F746F627566420A456D70747950726F746F50015A057479706573F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33"
    };
}
