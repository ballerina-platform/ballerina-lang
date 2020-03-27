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
import ballerina/time;

HealthServerBlockingClient healthServicelockingEp = new ("http://localhost:9113");

// Uncomment when you need to run this locally
// public function main(string... args) {
//     io:println(testRegisterPatient());
//     io:println(testGetPatientInfo());
// }

public function testRegisterPatient() returns boolean|grpc:Error {
    time:Time registerDealine = time:currentTime();
    registerDealine = time:addDuration(registerDealine, 0, 0, 0, 0, 10, 0, 0);

    grpc:ClientContext registerContext = new;
    registerContext.setDeadline(registerDealine);
    Patient patient = {
        id: "a001",
        name: "Ann Robinson",
        disease: "Migraine"
    };

    var registerResponse = healthServicelockingEp->registerPatient(patient, registerContext);
    if (registerResponse is grpc:Error) {
        return registerResponse;
    } else {
        boolean isRegisterSucceed = false;
        [isRegisterSucceed, _] = registerResponse;
        return isRegisterSucceed;
    }
}

public function testGetPatientInfo() returns boolean|grpc:Error {
    string patientId = "a002";
    time:Time getInfoDealine = time:currentTime();
    getInfoDealine = time:subtractDuration(getInfoDealine, 0, 0, 0, 0, 10, 0, 0);

    grpc:ClientContext getInfoContext = new;
    getInfoContext.setDeadline(getInfoDealine);

    var infoResponse = healthServicelockingEp->getPatientInfo(patientId, getInfoContext);
    if (infoResponse is grpc:Error) {
        return infoResponse;
    }
    return true;
}

public type HealthServerBlockingClient client object {

    *grpc:AbstractClientEndpoint;

    private grpc:Client grpcClient;

    public function __init(string url, grpc:ClientConfiguration? config = ()) {
        // initialize client endpoint.
        self.grpcClient = new(url, config);
        checkpanic self.grpcClient.initStub(self, "blocking", ROOT_DESCRIPTOR, getDescriptorMap());
    }

    public remote function registerPatient(Patient req, grpc:ClientContext? context = ()) returns ([boolean, grpc:Headers]|grpc:Error) {

        grpc:Headers? headers = ();
        if context is grpc:ClientContext {
            headers = context.getContextHeaders();
        }
        var payload = check self.grpcClient->blockingExecute("HealthServer/registerPatient", req, headers);
        grpc:Headers resHeaders = new;
        anydata result = ();
        [result, resHeaders] = payload;

        return [<boolean>result, resHeaders];

    }

    public remote function getPatientInfo(string req, grpc:ClientContext? context = ()) returns ([Patient, grpc:Headers]|grpc:Error) {

        grpc:Headers? headers = ();
        if context is grpc:ClientContext {
            headers = context.getContextHeaders();
        }
        var payload = check self.grpcClient->blockingExecute("HealthServer/getPatientInfo", req, headers);
        grpc:Headers resHeaders = new;
        anydata result = ();
        [result, resHeaders] = payload;

        return [<Patient>result, resHeaders];

    }

};

public type HealthServerClient client object {

    *grpc:AbstractClientEndpoint;

    private grpc:Client grpcClient;

    public function __init(string url, grpc:ClientConfiguration? config = ()) {
        // initialize client endpoint.
        self.grpcClient = new(url, config);
        checkpanic self.grpcClient.initStub(self, "non-blocking", ROOT_DESCRIPTOR, getDescriptorMap());
    }

    public remote function registerPatient(Patient req, service msgListener, grpc:ClientContext? context = ()) returns (grpc:Error?) {

        grpc:Headers? headers = ();
        if context is grpc:ClientContext {
            headers = context.getContextHeaders();
        }
        return self.grpcClient->nonBlockingExecute("HealthServer/registerPatient", req, msgListener, headers);
    }

    public remote function getPatientInfo(string req, service msgListener, grpc:ClientContext? context = ()) returns (grpc:Error?) {

        grpc:Headers? headers = ();
        if context is grpc:ClientContext {
            headers = context.getContextHeaders();
        }
        return self.grpcClient->nonBlockingExecute("HealthServer/getPatientInfo", req, msgListener, headers);
    }

};

public type Patient record {|
    string id = "";
    string name = "";
    string disease = "";

|};



const string ROOT_DESCRIPTOR = "0A116865616C74682D737475622E70726F746F1A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F22470A0750617469656E74120E0A0269641801200128095202696412120A046E616D6518022001280952046E616D6512180A07646973656173651803200128095207646973656173653281010A0C4865616C746853657276657212370A0F726567697374657250617469656E7412082E50617469656E741A1A2E676F6F676C652E70726F746F6275662E426F6F6C56616C756512380A0E67657450617469656E74496E666F121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A082E50617469656E74620670726F746F33";
function getDescriptorMap() returns map<string> {
    return {
        "health-stub.proto":"0A116865616C74682D737475622E70726F746F1A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F22470A0750617469656E74120E0A0269641801200128095202696412120A046E616D6518022001280952046E616D6512180A07646973656173651803200128095207646973656173653281010A0C4865616C746853657276657212370A0F726567697374657250617469656E7412082E50617469656E741A1A2E676F6F676C652E70726F746F6275662E426F6F6C56616C756512380A0E67657450617469656E74496E666F121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A082E50617469656E74620670726F746F33",
        "google/protobuf/wrappers.proto":"0A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F120F676F6F676C652E70726F746F62756622230A0B446F75626C6556616C756512140A0576616C7565180120012801520576616C756522220A0A466C6F617456616C756512140A0576616C7565180120012802520576616C756522220A0A496E74363456616C756512140A0576616C7565180120012803520576616C756522230A0B55496E74363456616C756512140A0576616C7565180120012804520576616C756522220A0A496E74333256616C756512140A0576616C7565180120012805520576616C756522230A0B55496E74333256616C756512140A0576616C756518012001280D520576616C756522210A09426F6F6C56616C756512140A0576616C7565180120012808520576616C756522230A0B537472696E6756616C756512140A0576616C7565180120012809520576616C756522220A0A427974657356616C756512140A0576616C756518012001280C520576616C756542570A13636F6D2E676F6F676C652E70726F746F627566420D577261707065727350726F746F50015A057479706573F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33"

    };
}

