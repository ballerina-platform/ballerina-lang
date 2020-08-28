// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/http;
import ballerina/runtime;
import ballerina/task;

listener http:Listener multipleServicesListener = new(15003);

const MULTI_SERVICE_SUCCESS_RESPONSE = "Multiple services invoked";
const LIMITED_RUNS_SUCCESS_RESPONSE = "Scheduler triggered limited times";
const FAILURE_RESPONSE = "Services failed to trigger";

public function main() {
    string cronExpression = "* * * * * ? *";
    task:Scheduler appointment = new({ appointmentDetails: cronExpression });

    checkpanic appointment.attach(service1);
    checkpanic appointment.attach(service2);
    checkpanic appointment.start();

    task:AppointmentConfiguration configuration = {
        appointmentDetails: cronExpression,
        noOfRecurrences: 3
    };

    task:Scheduler appointmentWithLimitedRuns = new(configuration);
    var result = appointmentWithLimitedRuns.attach(service3);
    checkpanic appointmentWithLimitedRuns.start();
}

boolean firstTriggered = false;
boolean secondTriggered = false;
int count1 = 0;
int count2 = 0;
int count3 = 0;

service service1 = service {
    resource function onTrigger() {
        count1 += 1;
        if (count1 > 3) {
            firstTriggered = true;
        }
    }
};

service service2 = service {
    resource function onTrigger() {
        count2 += 1;
        if (count2 > 3) {
            secondTriggered = true;
        }
    }
};

service service3 = service {
    resource function onTrigger() {
        count3 += 1;
    }
};

@http:ServiceConfig {
    basePath: "/"
}
service MultipleServiceListener on multipleServicesListener {
    @http:ResourceConfig {
        methods:["GET"]
    }
    resource function getMultipleServiceResult(http:Caller caller, http:Request request) {
        if (firstTriggered && secondTriggered) {
            var result = caller->respond(MULTI_SERVICE_SUCCESS_RESPONSE);
        } else {
            var result = caller->respond(FAILURE_RESPONSE);
        }
    }

    @http:ResourceConfig {
        methods:["GET"]
    }
    resource function getLimitedNumberOfRunsResult(http:Caller caller, http:Request request) {
        // Sleep for 5 seconds, as the scheduler is scheduled to run every second, for only three times.
        // Hence it should make count3 = 3, and then stop, after 3 seconds it was started.
        // We wait 5 seconds so that to confirm it did not ran more than 3 times.
        runtime:sleep(5000);
        if (count3 == 3) {
            var result = caller->respond(LIMITED_RUNS_SUCCESS_RESPONSE);
        } else {
            var result = caller->respond(FAILURE_RESPONSE);
        }
    }
}
