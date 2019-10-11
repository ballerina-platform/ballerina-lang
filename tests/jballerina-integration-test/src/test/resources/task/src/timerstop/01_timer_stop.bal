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

const SUCCESS_RESPONSE = "Task successfully stopped";
const FAILURE_RESPONSE = "Stopping the task failed";

listener http:Listener timerStopListener = new(15006);

public function main() {
    task:TimerConfiguration configuration = {
        intervalInMillis: 500,
        initialDelayInMillis: 1000
    };

    task:Scheduler timer = new(configuration);
    checkpanic timer.attach(timerService);
    checkpanic timer.start();
    runtime:sleep(4000);
    checkpanic  timer.stop();
}

int count = 0;

service timerService = service {
    resource function onTrigger() {
        count = count + 1;
    }
};

@http:ServiceConfig {
    basePath: "/"
}
service TimerStopResultService on timerStopListener {
    @http:ResourceConfig {
        methods: ["GET"]
    }
    resource function getTimerStopResult(http:Caller caller, http:Request request) {
        // Wait for 5 seconds to check if the counter goes above 5. If it does not stopped, the count should be greater
        // than 5 at least, as we stay 4 seconds after starting the scheduler.
        runtime:sleep(5000);
        if (count > 5) {
            var result = caller->respond(SUCCESS_RESPONSE);
        } else {
            var result = caller->respond(FAILURE_RESPONSE);
        }
    }
}
