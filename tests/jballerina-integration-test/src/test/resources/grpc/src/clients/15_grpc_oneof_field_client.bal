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

OneofFieldServiceBlockingClient blockingEp = new("http://localhost:9105");
const string ERROR_MESSAGE = "Expected response value type not received";

// Enable when you need to test locally.
//public function main() {
//    string resp;
//    resp = testOneofFieldValue();
//    io:println(resp);
//    resp = testDoubleFieldValue();
//    io:println(resp);
//    resp = testFloatFieldValue();
//    io:println(resp);
//    resp = testInt64FieldValue();
//    io:println(resp);
//    resp = testUInt64FieldValue();
//    io:println(resp);
//    resp = testInt32FieldValue();
//    io:println(resp);
//    resp = testFixed64FieldValue();
//    io:println(resp);
//    resp = testFixed32FieldValue();
//    io:println(resp);
//    resp = testBolFieldValue();
//    io:println(resp);
//    resp = testStringFieldValue();
//    io:println(resp);
//    resp = testMessageFieldValue();
//    io:println(resp);
//    resp = testBytesFieldValue();
//    io:println(resp);
//}

public function testOneofFieldValue() returns string {
    Request1 request = {first_name:"Sam", age:31};
    var result = blockingEp->hello(request);
    if (result is grpc:Error) {
        return io:sprintf("Error from Connector: %s ", result.message());
    } else {
        Response1 resp = {message:""};
        [resp, _] = result;

        return resp.message;
    }
}

public function testDoubleFieldValue() returns string {
    ZZZ zzz = {one_a:1.7976931348623157E308};
    var result = blockingEp->testOneofField(zzz);
    if (result is grpc:Error) {
        return io:sprintf("Error from Connector: %s", result.message());
    } else {
        ZZZ resp;
        [resp, _] = result;
        return resp?.one_a.toString();
    }
}

public function testFloatFieldValue() returns string {
    ZZZ zzz = {one_b:3.4028235E38};
    var result = blockingEp->testOneofField(zzz);
    if (result is grpc:Error) {
        return io:sprintf("Error from Connector: %s", result.message());
    } else {
        ZZZ resp;
        [resp, _] = result;
        return resp?.one_b.toString();
    }
}

public function testInt64FieldValue() returns string {
    ZZZ zzz = {one_c:-9223372036854775808};
    var result = blockingEp->testOneofField(zzz);
    if (result is grpc:Error) {
        return io:sprintf("Error from Connector: %s", result.message());
    } else {
        ZZZ resp;
        [resp, _] = result;
        return resp?.one_c.toString();
    }
}

public function testUInt64FieldValue() returns string {
    ZZZ zzz = {one_d:9223372036854775807};
    var result = blockingEp->testOneofField(zzz);
    if (result is grpc:Error) {
        return io:sprintf("Error from Connector: %s", result.message());
    } else {
        ZZZ resp;
        [resp, _] = result;
        return resp?.one_d.toString();
    }
}

public function testInt32FieldValue() returns string {
    ZZZ zzz = {one_e:-2147483648};
    var result = blockingEp->testOneofField(zzz);
    if (result is grpc:Error) {
        return io:sprintf("Error from Connector: %s", result.message());
    } else {
        ZZZ resp;
        [resp, _] = result;
        return resp?.one_e.toString();
    }
}

public function testFixed64FieldValue() returns string {
    ZZZ zzz = {one_f:9223372036854775807};
    var result = blockingEp->testOneofField(zzz);
    if (result is grpc:Error) {
        return io:sprintf("Error from Connector: %s", result.message());
    } else {
        ZZZ resp;
        [resp, _] = result;
        return resp?.one_f.toString();
    }
}

public function testFixed32FieldValue() returns string {
    ZZZ zzz = {one_g:2147483647};
    var result = blockingEp->testOneofField(zzz);
    if (result is grpc:Error) {
        return io:sprintf("Error from Connector: %s", result.message());
    } else {
        ZZZ resp;
        [resp, _] = result;
        return resp?.one_g.toString();
    }
}

public function testBolFieldValue() returns string {
    ZZZ zzz = {one_h:true};
    var result = blockingEp->testOneofField(zzz);
    if (result is grpc:Error) {
        return io:sprintf("Error from Connector: %s", result.message());
    } else {
        ZZZ resp;
        [resp, _] = result;
        return resp?.one_h.toString();
    }
}

public function testStringFieldValue() returns string {
    ZZZ zzz = {one_i:"Testing"};
    var result = blockingEp->testOneofField(zzz);
    if (result is grpc:Error) {
        return io:sprintf("Error from Connector: %s", result.message());
    } else {
        ZZZ resp;
        [resp, _] = result;
        return resp?.one_i.toString();
    }
}

public function testMessageFieldValue() returns string {
    AAA aaa = {aaa: "Testing"};
    ZZZ zzz = {one_j:aaa};
    var result = blockingEp->testOneofField(zzz);
    if (result is grpc:Error) {
        return io:sprintf("Error from Connector: %s", result.message());
    } else {
        ZZZ resp;
        [resp, _] = result;
        return resp?.one_j?.aaa.toString();
    }
}

public function testBytesFieldValue() returns string {
    string statement = "Lion in Town.";
    byte[] bytes = statement.toBytes();
    ZZZ zzz = {one_k:bytes};
    var result = blockingEp->testOneofField(zzz);
    if (result is grpc:Error) {
        return io:sprintf("Error from Connector: %s", result.message());
    } else {
        ZZZ resp;
        [resp, _] = result;
        boolean bResp = resp?.one_k == bytes;
        return bResp.toString();
    }
}

public type OneofFieldServiceBlockingClient client object {

    *grpc:AbstractClientEndpoint;

    private grpc:Client grpcClient;

    public function __init(string url, grpc:ClientConfiguration? config = ()) {
        // initialize client endpoint.
        self.grpcClient = new(url, config);
        checkpanic self.grpcClient.initStub(self, "blocking", ROOT_DESCRIPTOR, getDescriptorMap());
    }

    public remote function hello(Request1 req, grpc:Headers? headers = ()) returns ([Response1, grpc:Headers]|grpc:Error) {
        var payload = check self.grpcClient->blockingExecute("grpcservices.OneofFieldService/hello", req, headers);
        grpc:Headers resHeaders = new;
        anydata result = ();
        [result, resHeaders] = payload;
        var value = result.cloneWithType(typedesc<Response1>);
        if (value is Response1) {
            return [value, resHeaders];
        } else {
            grpc:Error err = grpc:InternalError("Error while constructing the message", value);
            return err;
        }
    }

    public remote function testOneofField(ZZZ req, grpc:Headers? headers = ()) returns ([ZZZ, grpc:Headers]|grpc:Error) {
        var payload = check self.grpcClient->blockingExecute("grpcservices.OneofFieldService/testOneofField", req, headers);
        grpc:Headers resHeaders = new;
        anydata result = ();
        [result, resHeaders] = payload;
        var value = result.cloneWithType(typedesc<ZZZ>);
        if (value is ZZZ) {
            return [value, resHeaders];
        } else {
            grpc:Error err = grpc:InternalError("Error while constructing the message", value);
            return err;
        }
    }

};

public type OneofFieldServiceClient client object {

    *grpc:AbstractClientEndpoint;

    private grpc:Client grpcClient;

    public function __init(string url, grpc:ClientConfiguration? config = ()) {
        // initialize client endpoint.
        self.grpcClient = new(url, config);
        checkpanic self.grpcClient.initStub(self, "non-blocking", ROOT_DESCRIPTOR, getDescriptorMap());
    }

    public remote function hello(Request1 req, service msgListener, grpc:Headers? headers = ()) returns (grpc:Error?) {
        return self.grpcClient->nonBlockingExecute("grpcservices.OneofFieldService/hello", req, msgListener, headers);
    }

    public remote function testOneofField(ZZZ req, service msgListener, grpc:Headers? headers = ()) returns (grpc:Error?) {
        return self.grpcClient->nonBlockingExecute("grpcservices.OneofFieldService/testOneofField", req, msgListener, headers);
    }
};

public type Request1 record {|
    int age?;
    Address1 address?;
    boolean married?;
    string first_name?;
    string last_name?;

|};

function isValidRequest1(Request1 r) returns boolean {
    int otherCount = 0;
    if !(r?.age is ()) {
        otherCount += 1;
    }
    if !(r?.address is ()) {
        otherCount += 1;
    }
    if !(r?.married is ()) {
        otherCount += 1;
    }

    int nameCount = 0;
    if !(r?.first_name is ()) {
        nameCount += 1;
    }
    if !(r?.last_name is ()) {
        nameCount += 1;
    }

    if (otherCount > 1 || nameCount > 1 ) {
        return false;
    }
    return true;
}

function setRequest1_Age(Request1 r, int age) {
    r.age = age;
    _ = r.removeIfHasKey("address");
    _ = r.removeIfHasKey("married");

}
function setRequest1_Address(Request1 r, Address1 address) {
    r.address = address;
    _ = r.removeIfHasKey("age");
    _ = r.removeIfHasKey("married");

}
function setRequest1_Married(Request1 r, boolean married) {
    r.married = married;
    _ = r.removeIfHasKey("age");
    _ = r.removeIfHasKey("address");

}

function setRequest1_FirstName(Request1 r, string first_name) {
    r.first_name = first_name;
    _ = r.removeIfHasKey("last_name");

}
function setRequest1_LastName(Request1 r, string last_name) {
    r.last_name = last_name;
    _ = r.removeIfHasKey("first_name");

}




public type Address1 record {|
    int house_number?;
    int street_number?;

|};

function isValidAddress1(Address1 r) returns boolean {
    int codeCount = 0;
    if !(r?.house_number is ()) {
        codeCount += 1;
    }
    if !(r?.street_number is ()) {
        codeCount += 1;
    }

    if (codeCount > 1 ) {
        return false;
    }
    return true;
}

function setAddress1_HouseNumber(Address1 r, int house_number) {
    r.house_number = house_number;
    _ = r.removeIfHasKey("street_number");

}
function setAddress1_StreetNumber(Address1 r, int street_number) {
    r.street_number = street_number;
    _ = r.removeIfHasKey("house_number");

}




public type Response1 record {|
    string message = "";

|};


public type ZZZ record {|
    float aa = 0.0;
    float bb = 0.0;
    int cc = 0;
    int dd = 0;
    int ee = 0;
    int ff = 0;
    int gg = 0;
    boolean hh = false;
    string ii = "";
    AAA? jj = ();
    byte[] kk = [];
    float one_a?;
    float one_b?;
    int one_c?;
    int one_d?;
    int one_e?;
    int one_f?;
    int one_g?;
    boolean one_h?;
    string one_i?;
    AAA one_j?;
    byte[] one_k?;

|};

function isValidZzz(ZZZ r) returns boolean {
    int valueCount = 0;
    if !(r?.one_a is ()) {
        valueCount += 1;
    }
    if !(r?.one_b is ()) {
        valueCount += 1;
    }
    if !(r?.one_c is ()) {
        valueCount += 1;
    }
    if !(r?.one_d is ()) {
        valueCount += 1;
    }
    if !(r?.one_e is ()) {
        valueCount += 1;
    }
    if !(r?.one_f is ()) {
        valueCount += 1;
    }
    if !(r?.one_g is ()) {
        valueCount += 1;
    }
    if !(r?.one_h is ()) {
        valueCount += 1;
    }
    if !(r?.one_i is ()) {
        valueCount += 1;
    }
    if !(r?.one_j is ()) {
        valueCount += 1;
    }
    if !(r?.one_k is ()) {
        valueCount += 1;
    }

    if (valueCount > 1 ) {
        return false;
    }
    return true;
}

function setZZZ_OneA(ZZZ r, float one_a) {
    r.one_a = one_a;
    _ = r.removeIfHasKey("one_b");
    _ = r.removeIfHasKey("one_c");
    _ = r.removeIfHasKey("one_d");
    _ = r.removeIfHasKey("one_e");
    _ = r.removeIfHasKey("one_f");
    _ = r.removeIfHasKey("one_g");
    _ = r.removeIfHasKey("one_h");
    _ = r.removeIfHasKey("one_i");
    _ = r.removeIfHasKey("one_j");
    _ = r.removeIfHasKey("one_k");

}
function setZZZ_OneB(ZZZ r, float one_b) {
    r.one_b = one_b;
    _ = r.removeIfHasKey("one_a");
    _ = r.removeIfHasKey("one_c");
    _ = r.removeIfHasKey("one_d");
    _ = r.removeIfHasKey("one_e");
    _ = r.removeIfHasKey("one_f");
    _ = r.removeIfHasKey("one_g");
    _ = r.removeIfHasKey("one_h");
    _ = r.removeIfHasKey("one_i");
    _ = r.removeIfHasKey("one_j");
    _ = r.removeIfHasKey("one_k");

}
function setZZZ_OneC(ZZZ r, int one_c) {
    r.one_c = one_c;
    _ = r.removeIfHasKey("one_a");
    _ = r.removeIfHasKey("one_b");
    _ = r.removeIfHasKey("one_d");
    _ = r.removeIfHasKey("one_e");
    _ = r.removeIfHasKey("one_f");
    _ = r.removeIfHasKey("one_g");
    _ = r.removeIfHasKey("one_h");
    _ = r.removeIfHasKey("one_i");
    _ = r.removeIfHasKey("one_j");
    _ = r.removeIfHasKey("one_k");

}
function setZZZ_OneD(ZZZ r, int one_d) {
    r.one_d = one_d;
    _ = r.removeIfHasKey("one_a");
    _ = r.removeIfHasKey("one_b");
    _ = r.removeIfHasKey("one_c");
    _ = r.removeIfHasKey("one_e");
    _ = r.removeIfHasKey("one_f");
    _ = r.removeIfHasKey("one_g");
    _ = r.removeIfHasKey("one_h");
    _ = r.removeIfHasKey("one_i");
    _ = r.removeIfHasKey("one_j");
    _ = r.removeIfHasKey("one_k");

}
function setZZZ_OneE(ZZZ r, int one_e) {
    r.one_e = one_e;
    _ = r.removeIfHasKey("one_a");
    _ = r.removeIfHasKey("one_b");
    _ = r.removeIfHasKey("one_c");
    _ = r.removeIfHasKey("one_d");
    _ = r.removeIfHasKey("one_f");
    _ = r.removeIfHasKey("one_g");
    _ = r.removeIfHasKey("one_h");
    _ = r.removeIfHasKey("one_i");
    _ = r.removeIfHasKey("one_j");
    _ = r.removeIfHasKey("one_k");

}
function setZZZ_OneF(ZZZ r, int one_f) {
    r.one_f = one_f;
    _ = r.removeIfHasKey("one_a");
    _ = r.removeIfHasKey("one_b");
    _ = r.removeIfHasKey("one_c");
    _ = r.removeIfHasKey("one_d");
    _ = r.removeIfHasKey("one_e");
    _ = r.removeIfHasKey("one_g");
    _ = r.removeIfHasKey("one_h");
    _ = r.removeIfHasKey("one_i");
    _ = r.removeIfHasKey("one_j");
    _ = r.removeIfHasKey("one_k");

}
function setZZZ_OneG(ZZZ r, int one_g) {
    r.one_g = one_g;
    _ = r.removeIfHasKey("one_a");
    _ = r.removeIfHasKey("one_b");
    _ = r.removeIfHasKey("one_c");
    _ = r.removeIfHasKey("one_d");
    _ = r.removeIfHasKey("one_e");
    _ = r.removeIfHasKey("one_f");
    _ = r.removeIfHasKey("one_h");
    _ = r.removeIfHasKey("one_i");
    _ = r.removeIfHasKey("one_j");
    _ = r.removeIfHasKey("one_k");

}
function setZZZ_OneH(ZZZ r, boolean one_h) {
    r.one_h = one_h;
    _ = r.removeIfHasKey("one_a");
    _ = r.removeIfHasKey("one_b");
    _ = r.removeIfHasKey("one_c");
    _ = r.removeIfHasKey("one_d");
    _ = r.removeIfHasKey("one_e");
    _ = r.removeIfHasKey("one_f");
    _ = r.removeIfHasKey("one_g");
    _ = r.removeIfHasKey("one_i");
    _ = r.removeIfHasKey("one_j");
    _ = r.removeIfHasKey("one_k");

}
function setZZZ_OneI(ZZZ r, string one_i) {
    r.one_i = one_i;
    _ = r.removeIfHasKey("one_a");
    _ = r.removeIfHasKey("one_b");
    _ = r.removeIfHasKey("one_c");
    _ = r.removeIfHasKey("one_d");
    _ = r.removeIfHasKey("one_e");
    _ = r.removeIfHasKey("one_f");
    _ = r.removeIfHasKey("one_g");
    _ = r.removeIfHasKey("one_h");
    _ = r.removeIfHasKey("one_j");
    _ = r.removeIfHasKey("one_k");

}
function setZZZ_OneJ(ZZZ r, AAA one_j) {
    r.one_j = one_j;
    _ = r.removeIfHasKey("one_a");
    _ = r.removeIfHasKey("one_b");
    _ = r.removeIfHasKey("one_c");
    _ = r.removeIfHasKey("one_d");
    _ = r.removeIfHasKey("one_e");
    _ = r.removeIfHasKey("one_f");
    _ = r.removeIfHasKey("one_g");
    _ = r.removeIfHasKey("one_h");
    _ = r.removeIfHasKey("one_i");
    _ = r.removeIfHasKey("one_k");

}
function setZZZ_OneK(ZZZ r, byte[] one_k) {
    r.one_k = one_k;
    _ = r.removeIfHasKey("one_a");
    _ = r.removeIfHasKey("one_b");
    _ = r.removeIfHasKey("one_c");
    _ = r.removeIfHasKey("one_d");
    _ = r.removeIfHasKey("one_e");
    _ = r.removeIfHasKey("one_f");
    _ = r.removeIfHasKey("one_g");
    _ = r.removeIfHasKey("one_h");
    _ = r.removeIfHasKey("one_i");
    _ = r.removeIfHasKey("one_j");

}




public type AAA record {|
    string aaa = "";

|};



const string ROOT_DESCRIPTOR = "0A0C6F6E656F66312E70726F746F120C67727063736572766963657322BF010A085265717565737431121F0A0A66697273745F6E616D651801200128094800520966697273744E616D65121D0A096C6173745F6E616D65180220012809480052086C6173744E616D6512120A036167651803200128054801520361676512320A076164647265737318042001280B32162E6772706373657276696365732E41646472657373314801520761646472657373121A0A076D617272696564180520012808480152076D61727269656442060A046E616D6542070A056F74686572225E0A08416464726573733112230A0C686F7573655F6E756D6265721801200128034800520B686F7573654E756D62657212250A0D7374726565745F6E756D6265721802200128074800520C7374726565744E756D62657242060A04636F646522250A09526573706F6E73653112180A076D65737361676518012001280952076D65737361676522E1030A035A5A5A12150A056F6E655F61180120012801480052046F6E654112150A056F6E655F62180220012802480052046F6E654212150A056F6E655F63180320012803480052046F6E654312150A056F6E655F64180420012804480052046F6E654412150A056F6E655F65180520012805480052046F6E654512150A056F6E655F66180620012806480052046F6E654612150A056F6E655F67180720012807480052046F6E654712150A056F6E655F68180820012808480052046F6E654812150A056F6E655F69180920012809480052046F6E654912280A056F6E655F6A180A2001280B32112E6772706373657276696365732E414141480052046F6E654A12150A056F6E655F6B180B2001280C480052046F6E654B120E0A026161180C2001280152026161120E0A026262180D2001280252026262120E0A026363180E2001280352026363120E0A026464180F2001280452026464120E0A02656518102001280552026565120E0A02666618112001280652026666120E0A02676718122001280752026767120E0A02686818132001280852026868120E0A0269691814200128095202696912210A026A6A18152001280B32112E6772706373657276696365732E41414152026A6A120E0A026B6B18162001280C52026B6B42070A0576616C756522170A0341414112100A0361616118012001280952036161613285010A114F6E656F664669656C645365727669636512380A0568656C6C6F12162E6772706373657276696365732E52657175657374311A172E6772706373657276696365732E526573706F6E73653112360A0E746573744F6E656F664669656C6412112E6772706373657276696365732E5A5A5A1A112E6772706373657276696365732E5A5A5A620670726F746F33";
function getDescriptorMap() returns map<string> {
    return {
        "oneof1.proto":"0A0C6F6E656F66312E70726F746F120C67727063736572766963657322BF010A085265717565737431121F0A0A66697273745F6E616D651801200128094800520966697273744E616D65121D0A096C6173745F6E616D65180220012809480052086C6173744E616D6512120A036167651803200128054801520361676512320A076164647265737318042001280B32162E6772706373657276696365732E41646472657373314801520761646472657373121A0A076D617272696564180520012808480152076D61727269656442060A046E616D6542070A056F74686572225E0A08416464726573733112230A0C686F7573655F6E756D6265721801200128034800520B686F7573654E756D62657212250A0D7374726565745F6E756D6265721802200128074800520C7374726565744E756D62657242060A04636F646522250A09526573706F6E73653112180A076D65737361676518012001280952076D65737361676522E1030A035A5A5A12150A056F6E655F61180120012801480052046F6E654112150A056F6E655F62180220012802480052046F6E654212150A056F6E655F63180320012803480052046F6E654312150A056F6E655F64180420012804480052046F6E654412150A056F6E655F65180520012805480052046F6E654512150A056F6E655F66180620012806480052046F6E654612150A056F6E655F67180720012807480052046F6E654712150A056F6E655F68180820012808480052046F6E654812150A056F6E655F69180920012809480052046F6E654912280A056F6E655F6A180A2001280B32112E6772706373657276696365732E414141480052046F6E654A12150A056F6E655F6B180B2001280C480052046F6E654B120E0A026161180C2001280152026161120E0A026262180D2001280252026262120E0A026363180E2001280352026363120E0A026464180F2001280452026464120E0A02656518102001280552026565120E0A02666618112001280652026666120E0A02676718122001280752026767120E0A02686818132001280852026868120E0A0269691814200128095202696912210A026A6A18152001280B32112E6772706373657276696365732E41414152026A6A120E0A026B6B18162001280C52026B6B42070A0576616C756522170A0341414112100A0361616118012001280952036161613285010A114F6E656F664669656C645365727669636512380A0568656C6C6F12162E6772706373657276696365732E52657175657374311A172E6772706373657276696365732E526573706F6E73653112360A0E746573744F6E656F664669656C6412112E6772706373657276696365732E5A5A5A1A112E6772706373657276696365732E5A5A5A620670726F746F33"

    };
}
