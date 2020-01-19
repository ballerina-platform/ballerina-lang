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
    Request1_FirstName first = {first_name:"Sam"};
    Request1_Age age = {age:31};
    Request1 request = {name:first, other:age};
    var result = blockingEp->hello(request);
    if (result is grpc:Error) {
        return io:sprintf("Error from Connector: %s - %s", result.reason(), <string> result.detail()["message"]);
    } else {
        Response1 resp = {message:""};
        [resp, _] = result;
        return resp.message;
    }
}

public function testDoubleFieldValue() returns string {
    ZZZ_OneA oneA = {one_a:1.7976931348623157E308};
    ZZZ zzz = {value:oneA};
    var result = blockingEp->testOneofField(zzz);
    if (result is grpc:Error) {
        return io:sprintf("Error from Connector: %s - %s", result.reason(), <string> result.detail()["message"]);
    } else {
        ZZZ resp;
        [resp, _] = result;
        Value val = resp.value;
        if (val is ZZZ_OneA) {
            return val.one_a.toString();
        }
        return ERROR_MESSAGE;
    }
}

public function testFloatFieldValue() returns string {
    ZZZ_OneB oneB = {one_b:3.4028235E38};
    ZZZ zzz = {value:oneB};
    var result = blockingEp->testOneofField(zzz);
    if (result is grpc:Error) {
        return io:sprintf("Error from Connector: %s - %s", result.reason(), <string> result.detail()["message"]);
    } else {
        ZZZ resp;
        [resp, _] = result;
        Value val = resp.value;
        if (val is ZZZ_OneB) {
            return val.one_b.toString();
        }
        return ERROR_MESSAGE;
    }
}

public function testInt64FieldValue() returns string {
    ZZZ_OneC oneC = {one_c:-9223372036854775808};
    ZZZ zzz = {value:oneC};
    var result = blockingEp->testOneofField(zzz);
    if (result is grpc:Error) {
        return io:sprintf("Error from Connector: %s - %s", result.reason(), <string> result.detail()["message"]);
    } else {
        ZZZ resp;
        [resp, _] = result;
        Value val = resp.value;
        if (val is ZZZ_OneC) {
            return val.one_c.toString();
        }
        return ERROR_MESSAGE;
    }
}

public function testUInt64FieldValue() returns string {
    ZZZ_OneD oneD = {one_d:9223372036854775807};
    ZZZ zzz = {value:oneD};
    var result = blockingEp->testOneofField(zzz);
    if (result is grpc:Error) {
        return io:sprintf("Error from Connector: %s - %s", result.reason(), <string> result.detail()["message"]);
    } else {
        ZZZ resp;
        [resp, _] = result;
        Value val = resp.value;
        if (val is ZZZ_OneD) {
            return val.one_d.toString();
        }
        return ERROR_MESSAGE;
    }
}

public function testInt32FieldValue() returns string {
    ZZZ_OneE oneE = {one_e:-2147483648};
    ZZZ zzz = {value:oneE};
    var result = blockingEp->testOneofField(zzz);
    if (result is grpc:Error) {
        return io:sprintf("Error from Connector: %s - %s", result.reason(), <string> result.detail()["message"]);
    } else {
        ZZZ resp;
        [resp, _] = result;
        Value val = resp.value;
        if (val is ZZZ_OneE) {
            return val.one_e.toString();
        }
        return ERROR_MESSAGE;
    }
}

public function testFixed64FieldValue() returns string {
    ZZZ_OneF oneF = {one_f:9223372036854775807};
    ZZZ zzz = {value:oneF};
    var result = blockingEp->testOneofField(zzz);
    if (result is grpc:Error) {
        return io:sprintf("Error from Connector: %s - %s", result.reason(), <string> result.detail()["message"]);
    } else {
        ZZZ resp;
        [resp, _] = result;
        Value val = resp.value;
        if (val is ZZZ_OneF) {
            return val.one_f.toString();
        }
        return ERROR_MESSAGE;
    }
}

public function testFixed32FieldValue() returns string {
    ZZZ_OneG oneG = {one_g:2147483647};
    ZZZ zzz = {value:oneG};
    var result = blockingEp->testOneofField(zzz);
    if (result is grpc:Error) {
        return io:sprintf("Error from Connector: %s - %s", result.reason(), <string> result.detail()["message"]);
    } else {
        ZZZ resp;
        [resp, _] = result;
        Value val = resp.value;
        if (val is ZZZ_OneG) {
            return val.one_g.toString();
        }
        return ERROR_MESSAGE;
    }
}

public function testBolFieldValue() returns string {
    ZZZ_OneH oneH = {one_h:true};
    ZZZ zzz = {value:oneH};
    var result = blockingEp->testOneofField(zzz);
    if (result is grpc:Error) {
        return io:sprintf("Error from Connector: %s - %s", result.reason(), <string> result.detail()["message"]);
    } else {
        ZZZ resp;
        [resp, _] = result;
        Value val = resp.value;
        if (val is ZZZ_OneH) {
            return val.one_h.toString();
        }
        return ERROR_MESSAGE;
    }
}

public function testStringFieldValue() returns string {
    ZZZ_OneI oneI = {one_i:"Testing"};
    ZZZ zzz = {value:oneI};
    var result = blockingEp->testOneofField(zzz);
    if (result is grpc:Error) {
        return io:sprintf("Error from Connector: %s - %s", result.reason(), <string> result.detail()["message"]);
    } else {
        ZZZ resp;
        [resp, _] = result;
        Value val = resp.value;
        if (val is ZZZ_OneI) {
            return val.one_i.toString();
        }
        return ERROR_MESSAGE;
    }
}

public function testMessageFieldValue() returns string {
    AAA aaa = {aaa: "Testing"};
    ZZZ_OneJ oneJ = {one_j:aaa};
    ZZZ zzz = {value:oneJ};
    var result = blockingEp->testOneofField(zzz);
    if (result is grpc:Error) {
        return io:sprintf("Error from Connector: %s - %s", result.reason(), <string> result.detail()["message"]);
    } else {
        ZZZ resp;
        [resp, _] = result;
        Value val = resp.value;
        if (val is ZZZ_OneJ) {
            return val.one_j.aaa;
        }
        return ERROR_MESSAGE;
    }
}

public function testBytesFieldValue() returns string {
    string statement = "Lion in Town.";
    byte[] bytes = statement.toBytes();
    ZZZ_OneK oneK = {one_k:bytes};
    ZZZ zzz = {value:oneK};
    var result = blockingEp->testOneofField(zzz);
    if (result is grpc:Error) {
        return io:sprintf("Error from Connector: %s - %s", result.reason(), <string> result.detail()["message"]);
    } else {
        ZZZ resp;
        [resp, _] = result;
        Value val = resp.value;
        if (val is ZZZ_OneK) {
            boolean bResp = val.one_k == bytes;
            return bResp.toString();
        }
        return ERROR_MESSAGE;
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
        var value = typedesc<Response1>.constructFrom(result);
        if (value is Response1) {
            return [value, resHeaders];
        } else {
            grpc:Error err = grpc:prepareError(grpc:INTERNAL_ERROR, "Error while constructing the message", value);
            return err;
        }
    }

    public remote function testOneofField(ZZZ req, grpc:Headers? headers = ()) returns ([ZZZ, grpc:Headers]|grpc:Error) {
        var payload = check self.grpcClient->blockingExecute("grpcservices.OneofFieldService/testOneofField", req, headers);
        grpc:Headers resHeaders = new;
        anydata result = ();
        [result, resHeaders] = payload;
        var value = typedesc<ZZZ>.constructFrom(result);
        if (value is ZZZ) {
            return [value, resHeaders];
        } else {
            grpc:Error err = grpc:prepareError(grpc:INTERNAL_ERROR, "Error while constructing the message", value);
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

public type Request1 record {
    Other other;
    Name name;
};

public type Other Request1_Age|Request1_Address|Request1_Married;

type Request1_Age record {
    int age;

};

type Request1_Address record {
    Address1 address;

};

type Request1_Married record {
    boolean married;

};

public type Name Request1_FirstName|Request1_LastName;

type Request1_FirstName record {
    string first_name;

};

type Request1_LastName record {
    string last_name;

};

type Address1 record {
    Code code;
};

public type Code Address1_HouseNumber|Address1_StreetNumber;

type Address1_HouseNumber record {
    int house_number;

};

type Address1_StreetNumber record {
    int street_number;

};

type Response1 record {
    string message;

};

public type ZZZ record {
    float aa = 1.2345;
    float bb = 1.23;
    int cc = 10;
    int dd = 11;
    int ee = 12;
    int ff = 13;
    int gg = 14;
    boolean hh = true;
    string ii = "Test";
    AAA jj = {};
    byte[] kk = [24, 25];
    Value value;
};

public type Value ZZZ_OneA|ZZZ_OneB|ZZZ_OneC|ZZZ_OneD|ZZZ_OneE|ZZZ_OneF|ZZZ_OneG|ZZZ_OneH|ZZZ_OneI|ZZZ_OneJ|ZZZ_OneK;

type ZZZ_OneA record {
    float one_a;

};

type ZZZ_OneB record {
    float one_b;

};

type ZZZ_OneC record {
    int one_c;

};

type ZZZ_OneD record {
    int one_d;

};

type ZZZ_OneE record {
    int one_e;

};

type ZZZ_OneF record {
    int one_f;

};

type ZZZ_OneG record {
    int one_g;

};

type ZZZ_OneH record {
    boolean one_h;

};

type ZZZ_OneI record {
    string one_i;

};

type ZZZ_OneJ record {
    AAA one_j;

};

type ZZZ_OneK record {
    byte[] one_k;

};

public type AAA record {
    string aaa = "aaa";
};

const string ROOT_DESCRIPTOR = "0A196F6E656F665F6669656C645F736572766963652E70726F746F120C67727063736572766963657322BF010A085265717565737431121F0A0A66697273745F6E616D651801200128094800520966697273744E616D65121D0A096C6173745F6E616D65180220012809480052086C6173744E616D6512120A036167651803200128054801520361676512320A076164647265737318042001280B32162E6772706373657276696365732E41646472657373314801520761646472657373121A0A076D617272696564180520012808480152076D61727269656442060A046E616D6542070A056F74686572225E0A08416464726573733112230A0C686F7573655F6E756D6265721801200128034800520B686F7573654E756D62657212250A0D7374726565745F6E756D6265721802200128074800520C7374726565744E756D62657242060A04636F646522250A09526573706F6E73653112180A076D65737361676518012001280952076D65737361676522E1030A035A5A5A12150A056F6E655F61180120012801480052046F6E654112150A056F6E655F62180220012802480052046F6E654212150A056F6E655F63180320012803480052046F6E654312150A056F6E655F64180420012804480052046F6E654412150A056F6E655F65180520012805480052046F6E654512150A056F6E655F66180620012806480052046F6E654612150A056F6E655F67180720012807480052046F6E654712150A056F6E655F68180820012808480052046F6E654812150A056F6E655F69180920012809480052046F6E654912280A056F6E655F6A180A2001280B32112E6772706373657276696365732E414141480052046F6E654A12150A056F6E655F6B180B2001280C480052046F6E654B120E0A026161180C2001280152026161120E0A026262180D2001280252026262120E0A026363180E2001280352026363120E0A026464180F2001280452026464120E0A02656518102001280552026565120E0A02666618112001280652026666120E0A02676718122001280752026767120E0A02686818132001280852026868120E0A0269691814200128095202696912210A026A6A18152001280B32112E6772706373657276696365732E41414152026A6A120E0A026B6B18162001280C52026B6B42070A0576616C756522170A0341414112100A0361616118012001280952036161613285010A114F6E656F664669656C645365727669636512380A0568656C6C6F12162E6772706373657276696365732E52657175657374311A172E6772706373657276696365732E526573706F6E73653112360A0E746573744F6E656F664669656C6412112E6772706373657276696365732E5A5A5A1A112E6772706373657276696365732E5A5A5A620670726F746F33";
function getDescriptorMap() returns map<string> {
    return {
        "oneof_field_service.proto":"0A196F6E656F665F6669656C645F736572766963652E70726F746F120C67727063736572766963657322BF010A085265717565737431121F0A0A66697273745F6E616D651801200128094800520966697273744E616D65121D0A096C6173745F6E616D65180220012809480052086C6173744E616D6512120A036167651803200128054801520361676512320A076164647265737318042001280B32162E6772706373657276696365732E41646472657373314801520761646472657373121A0A076D617272696564180520012808480152076D61727269656442060A046E616D6542070A056F74686572225E0A08416464726573733112230A0C686F7573655F6E756D6265721801200128034800520B686F7573654E756D62657212250A0D7374726565745F6E756D6265721802200128074800520C7374726565744E756D62657242060A04636F646522250A09526573706F6E73653112180A076D65737361676518012001280952076D65737361676522E1030A035A5A5A12150A056F6E655F61180120012801480052046F6E654112150A056F6E655F62180220012802480052046F6E654212150A056F6E655F63180320012803480052046F6E654312150A056F6E655F64180420012804480052046F6E654412150A056F6E655F65180520012805480052046F6E654512150A056F6E655F66180620012806480052046F6E654612150A056F6E655F67180720012807480052046F6E654712150A056F6E655F68180820012808480052046F6E654812150A056F6E655F69180920012809480052046F6E654912280A056F6E655F6A180A2001280B32112E6772706373657276696365732E414141480052046F6E654A12150A056F6E655F6B180B2001280C480052046F6E654B120E0A026161180C2001280152026161120E0A026262180D2001280252026262120E0A026363180E2001280352026363120E0A026464180F2001280452026464120E0A02656518102001280552026565120E0A02666618112001280652026666120E0A02676718122001280752026767120E0A02686818132001280852026868120E0A0269691814200128095202696912210A026A6A18152001280B32112E6772706373657276696365732E41414152026A6A120E0A026B6B18162001280C52026B6B42070A0576616C756522170A0341414112100A0361616118012001280952036161613285010A114F6E656F664669656C645365727669636512380A0568656C6C6F12162E6772706373657276696365732E52657175657374311A172E6772706373657276696365732E526573706F6E73653112360A0E746573744F6E656F664669656C6412112E6772706373657276696365732E5A5A5A1A112E6772706373657276696365732E5A5A5A620670726F746F33"

    };
}
