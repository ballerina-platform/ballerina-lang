// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

listener grpc:Listener ep23 = new (9113);

@grpc:ServiceConfig {name:"HealthServer"}
@grpc:ServiceDescriptor {
    descriptor: ROOT_DESCRIPTOR_23,
    descMap: getDescriptorMap23()
}
service HealthServer on ep23 {

    resource function registerPatient(grpc:Caller caller, Patient patient, grpc:Headers headers) {
        log:printInfo("Registration process begins");
        log:printInfo("Received patient information");
        log:printInfo("Patient name: " + patient.name);

        boolean deadlineExceeded = caller->isCancelled(headers);
        grpc:Error? result = ();
        if deadlineExceeded {
            result = caller->sendError(grpc:DEADLINE_EXCEEDED, "Service took more that allocated deadline to complete.");
        } else {
            result = caller->send(true);
            if (result is grpc:Error) {
                log:printError("Error occured during the registration: " + result.reason() + " - " + <string>result.detail()["message"]);
            }
            result = caller->complete();
            if (result is grpc:Error) {
                log:printError("Error occured during the registration: " + result.reason() + " - " + <string>result.detail()["message"]);
            }
        }
    }
    resource function getPatientInfo(grpc:Caller caller, string patientId, grpc:Headers headers) {
        log:printInfo("Information extraction process begins");
        log:printInfo("Patient ID: " + patientId);
        Patient patient = {
            id: "a002",
            name: "John Marshal",
            disease: "Headache"
        };

        boolean deadlineExceeded = caller->isCancelled(headers);
        grpc:Error? result = ();
        if deadlineExceeded {
            result = caller->sendError(grpc:DEADLINE_EXCEEDED, "Service took more that allocated deadline to complete.");
        } else {
            result = caller->send(patient);
            if (result is grpc:Error) {
                log:printError("Error occured during the information extraction: " + result.reason() + " - " + <string>result.detail()["message"]);
            }

            result = caller->complete();
            if (result is grpc:Error) {
                log:printError("Error occured during the information extraction: " + result.reason() + " - " + <string>result.detail()["message"]);
            }
        }
    }
}

public type Patient record {|
    string id = "";
    string name = "";
    string disease = "";
|};

const string ROOT_DESCRIPTOR_23 = "0A116865616C74682D737475622E70726F746F1A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F22470A0750617469656E74120E0A0269641801200128095202696412120A046E616D6518022001280952046E616D6512180A07646973656173651803200128095207646973656173653281010A0C4865616C746853657276657212370A0F726567697374657250617469656E7412082E50617469656E741A1A2E676F6F676C652E70726F746F6275662E426F6F6C56616C756512380A0E67657450617469656E74496E666F121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A082E50617469656E74620670726F746F33";
function getDescriptorMap23() returns map<string> {
    return {
        "health-stub.proto": "0A116865616C74682D737475622E70726F746F1A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F22470A0750617469656E74120E0A0269641801200128095202696412120A046E616D6518022001280952046E616D6512180A07646973656173651803200128095207646973656173653281010A0C4865616C746853657276657212370A0F726567697374657250617469656E7412082E50617469656E741A1A2E676F6F676C652E70726F746F6275662E426F6F6C56616C756512380A0E67657450617469656E74496E666F121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A082E50617469656E74620670726F746F33",
        "google/protobuf/wrappers.proto": "0A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F120F676F6F676C652E70726F746F62756622230A0B446F75626C6556616C756512140A0576616C7565180120012801520576616C756522220A0A466C6F617456616C756512140A0576616C7565180120012802520576616C756522220A0A496E74363456616C756512140A0576616C7565180120012803520576616C756522230A0B55496E74363456616C756512140A0576616C7565180120012804520576616C756522220A0A496E74333256616C756512140A0576616C7565180120012805520576616C756522230A0B55496E74333256616C756512140A0576616C756518012001280D520576616C756522210A09426F6F6C56616C756512140A0576616C7565180120012808520576616C756522230A0B537472696E6756616C756512140A0576616C7565180120012809520576616C756522220A0A427974657356616C756512140A0576616C756518012001280C520576616C756542570A13636F6D2E676F6F676C652E70726F746F627566420D577261707065727350726F746F50015A057479706573F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33"

    };
}
