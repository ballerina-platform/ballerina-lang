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

import ballerina/task;

public function triggerTimer() {
    int interval = 1000;
    task:Scheduler timer = new({ intervalInMillis: interval });

    checkpanic timer.attach(service1);
    checkpanic timer.attach(service2);
    checkpanic timer.start();
}

boolean firstTriggered = false;
boolean secondTriggered = false;
int count1 = 0;
int count2 = 0;

function getResult() returns boolean {
    return (firstTriggered && secondTriggered);
}

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
