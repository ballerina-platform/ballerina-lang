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

task:TimerConfiguration configuration = {
    intervalInMillis: 1000,
    initialDelayInMillis: 1000
};

const SUCCESS_RESPONSE = "Successfully paused and resumed";
const FAILURE_RESPONSE = "Pause and resume failed";

listener http:Listener timerPauseResumeListener = new(15005);

int counter1 = 0;
int counter2 = 0;

public function main() {
    task:Scheduler timer1 = new(configuration);
    task:Scheduler timer2 = new(configuration);
    checkpanic timer1.attach(timerService1);
    checkpanic timer2.attach(timerService2);
    checkpanic timer1.start();
    checkpanic timer2.start();
    var result = timer1.pause();
    if (result is error) {
        return;
    }
    runtime:sleep(6000);
    result = timer1.resume();
    if (result is error) {
        return;
    }
}

service timerService1 = service {
    resource function onTrigger() {
        counter1 = counter1 + 1;
    }
};

service timerService2 = service {
    resource function onTrigger() {
        counter2 = counter2 + 1;
    }
};

@http:ServiceConfig {
	basePath: "/"
}
service TimerAttachmentHttpService on timerPauseResumeListener {
    @http:ResourceConfig {
        methods:["GET"]
    }
    resource function getTaskPauseResumeResult(http:Caller caller, http:Request request) {
        runtime:sleep(4000);
		if (counter1 > 3 && (counter2 - counter1) > 4) {
		    var result = caller->respond(SUCCESS_RESPONSE);
		} else {
		    var result = caller->respond(FAILURE_RESPONSE);
		}
	}
}
