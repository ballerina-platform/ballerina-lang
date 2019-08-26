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

const MULTI_SERVICE_SUCCESS_RESPONSE = "Multiple services invoked";
const FAILURE_RESPONSE = "Services failed to trigger";

boolean firstTriggered = false;
boolean secondTriggered = false;

string result = "";

int count1 = 0;
int count2 = 0;

public type Person record {
    string name;
    int age;
};

listener http:Listener taskAttachmentListener = new(15004);

public function main() {
    Person person = {
        name: "Sam",
        age: 0
    };

    task:Scheduler timer = new({ intervalInMillis: 1000, initialDelayInMillis: 1000, noOfRecurrences: 5 });
    var attachResult = timer.attach(timerService, person);
    if (attachResult is task:SchedulerError) {
        panic attachResult;
    }
    var startResult = timer.start();
    if (startResult is task:SchedulerError) {
        panic startResult;
    }

    task:Scheduler timerWithMultipleServices = new({ intervalInMillis: 1000 });
    checkpanic timerWithMultipleServices.attach(service1);
    checkpanic timerWithMultipleServices.attach(service2);
    checkpanic timerWithMultipleServices.start();
}

service timerService = service {
    resource function onTrigger(Person person) {
        person.age = person.age + 1;
        result = <@untainted string> (person.name + " is " + person.age.toString() + " years old");
    }
};

service service1 = service {
    resource function onTrigger() {
        count1 = count1 + 1;
        if (count1 > 3) {
            firstTriggered = true;
        }
    }
};

service service2 = service {
    resource function onTrigger() {
        count2 = count2 + 1;
        if (count2 > 3) {
            secondTriggered = true;
        }
    }
};

@http:ServiceConfig {
	basePath: "/"
}
service TimerAttachmentHttpService on taskAttachmentListener {
    @http:ResourceConfig {
        methods: ["GET"]
    }
    resource function getTaskAttachmentResult(http:Caller caller, http:Request request) {
        // Sleep for 8 seconds to check whether the task is running for more than 5 times.
        runtime:sleep(8000);
		var sendResult = caller->respond(result);
	}

	@http:ResourceConfig {
	    methods: ["GET"]
	}
	resource function getMultipleServicesResult(http:Caller caller, http:Request request) {
	    if (firstTriggered && secondTriggered) {
            var result = caller->respond(MULTI_SERVICE_SUCCESS_RESPONSE);
        } else {
            var result = caller->respond(FAILURE_RESPONSE);
        }
	}
}
