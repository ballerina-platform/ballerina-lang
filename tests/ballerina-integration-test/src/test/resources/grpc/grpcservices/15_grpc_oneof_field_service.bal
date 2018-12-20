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

@grpc:ServiceDescriptor {
    descriptor: ROOT_DESCRIPTOR_15,
    descMap: getDescriptorMap15()
}
service OneofFieldService on new grpc:Listener(9105) {

    resource function hello(grpc:Caller caller, HelloRequest value) {
        string request = "";
        if (value.name is HelloRequest_FirstName) {
            var conv = HelloRequest_FirstName.convert(value.name);
            if (conv is HelloRequest_FirstName) {
                request = conv.first_name;
            }
        } else {
            HelloRequest_LastName|error conv = HelloRequest_LastName.convert(value.name);
            if (conv is HelloRequest_LastName) {
                request = conv.last_name;
            }
        }
        HelloResponse response = {message: "Hello " + request};
        _ = caller->send(response);
        _ = caller->complete();
    }
}

type HelloRequest record {
    Other other;Name name;
};

public type Other HelloRequest_Age|HelloRequest_Address|HelloRequest_Married;

type HelloRequest_Age record {
    int age;

};

type HelloRequest_Address record {
    Address1 address;

};

type HelloRequest_Married record {
    boolean married;

};

public type Name HelloRequest_FirstName|HelloRequest_LastName;

type HelloRequest_FirstName record {
    string first_name;

};

type HelloRequest_LastName record {
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

type HelloResponse record {
    string message;

};

const string ROOT_DESCRIPTOR_15 =
"0A196F6E656F665F6669656C645F736572766963652E70726F746F120C67727063736572766963657322C3010A0C48656C6C6F52657175657374121F0A0A66697273745F6E616D651801200128094800520966697273744E616D65121D0A096C6173745F6E616D65180220012809480052086C6173744E616D6512120A036167651803200128054801520361676512320A076164647265737318042001280B32162E6772706373657276696365732E41646472657373314801520761646472657373121A0A076D617272696564180520012808480152076D61727269656442060A046E616D6542070A056F74686572225E0A08416464726573733112230A0C686F7573655F6E756D6265721801200128034800520B686F7573654E756D62657212250A0D7374726565745F6E756D6265721802200128074800520C7374726565744E756D62657242060A04636F646522290A0D48656C6C6F526573706F6E736512180A076D65737361676518012001280952076D65737361676532550A114F6E656F664669656C645365727669636512400A0568656C6C6F121A2E6772706373657276696365732E48656C6C6F526571756573741A1B2E6772706373657276696365732E48656C6C6F526573706F6E7365620670726F746F33";
function getDescriptorMap15() returns map<string> {
    return {
        "oneof_field_service.proto":"0A196F6E656F665F6669656C645F736572766963652E70726F746F120C67727063736572766963657322C3010A0C48656C6C6F52657175657374121F0A0A66697273745F6E616D651801200128094800520966697273744E616D65121D0A096C6173745F6E616D65180220012809480052086C6173744E616D6512120A036167651803200128054801520361676512320A076164647265737318042001280B32162E6772706373657276696365732E41646472657373314801520761646472657373121A0A076D617272696564180520012808480152076D61727269656442060A046E616D6542070A056F74686572225E0A08416464726573733112230A0C686F7573655F6E756D6265721801200128034800520B686F7573654E756D62657212250A0D7374726565745F6E756D6265721802200128074800520C7374726565744E756D62657242060A04636F646522290A0D48656C6C6F526573706F6E736512180A076D65737361676518012001280952076D65737361676532550A114F6E656F664669656C645365727669636512400A0568656C6C6F121A2E6772706373657276696365732E48656C6C6F526571756573741A1B2E6772706373657276696365732E48656C6C6F526573706F6E7365620670726F746F33"

    };
}
