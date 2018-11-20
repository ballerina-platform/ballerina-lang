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
import ballerina/log;

endpoint grpc:Listener ep98 {
    host:"localhost",
    port:9098
};
@grpc:ServiceDescriptor {
    descriptor: <string>descriptorMap5[DESCRIPTOR_KEY_5],
    descMap: descriptorMap5
}
service HelloWorld98 bind ep98 {
    hello(endpoint caller, string name) {
        log:printInfo("name: " + name);
        string message = "Hello " + name;
        error? err = ();
        if (name == "invalid") {
            err = caller->sendError(grpc:ABORTED, "Operation aborted");
        } else {
            err = caller->send(message);
        }
        if (err is error) {
            log:printError(err.reason(), err = err);
        }
        _ = caller->complete();
    }

    testInt(endpoint caller, string age) {
        log:printInfo("age: " + age);
        int displayAge = 0;
        if (age == "") {
            displayAge = -1;
        } else {
            displayAge = 1;
        }
        error? err = caller->send(displayAge);
        if (err is error) {
            log:printError(err.reason(), err = err);
        } else {
            log:printInfo("display age : " + displayAge);
        }
        _ = caller->complete();
    }

    testFloat(endpoint caller, float salary) {
        log:printInfo("gross salary: " + salary);
        string netSalary = <string>(salary * 0.88);
        error? err = caller->send(netSalary);
        if (err is error) {
            log:printError(err.reason(), err = err);
        } else {
            log:printInfo("net salary : " + netSalary);
        }
        _ = caller->complete();
    }
}

@final string DESCRIPTOR_KEY_5 = "HelloWorld98.proto";
map descriptorMap5 =
{
    "HelloWorld98.proto":"0A1248656C6C6F576F726C6439382E70726F746F120C6772706373657276696365731A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F32E1010A0C48656C6C6F576F726C64393812430A0568656C6C6F121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C756512440A0774657374496E74121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A1B2E676F6F676C652E70726F746F6275662E496E74363456616C756512460A0974657374466C6F6174121B2E676F6F676C652E70726F746F6275662E466C6F617456616C75651A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C7565620670726F746F33",

    "google/protobuf/wrappers.proto":
    "0A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F120F676F6F676C652E70726F746F627566221C0A0B446F75626C6556616C7565120D0A0576616C7565180120012801221B0A0A466C6F617456616C7565120D0A0576616C7565180120012802221B0A0A496E74363456616C7565120D0A0576616C7565180120012803221C0A0B55496E74363456616C7565120D0A0576616C7565180120012804221B0A0A496E74333256616C7565120D0A0576616C7565180120012805221C0A0B55496E74333256616C7565120D0A0576616C756518012001280D221A0A09426F6F6C56616C7565120D0A0576616C7565180120012808221C0A0B537472696E6756616C7565120D0A0576616C7565180120012809221B0A0A427974657356616C7565120D0A0576616C756518012001280C427C0A13636F6D2E676F6F676C652E70726F746F627566420D577261707065727350726F746F50015A2A6769746875622E636F6D2F676F6C616E672F70726F746F6275662F7074797065732F7772617070657273F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33"
    ,

    "google/protobuf/empty.proto":
    "0A1B676F6F676C652F70726F746F6275662F656D7074792E70726F746F120F676F6F676C652E70726F746F62756622070A05456D70747942760A13636F6D2E676F6F676C652E70726F746F627566420A456D70747950726F746F50015A276769746875622E636F6D2F676F6C616E672F70726F746F6275662F7074797065732F656D707479F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33"

};
