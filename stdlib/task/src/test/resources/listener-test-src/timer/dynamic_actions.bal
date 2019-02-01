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
import ballerina/runtime;

boolean isPaused = false;
boolean isResumed = false;

service timerService = service {
    resource function onTrigger() {
        // Do nothing
    }

    resource function onError() {
        // Do nothing
    }
};

function testAttach() {
    task:TimerConfiguration configurations = getConfigurations(1000, 2000, 3);
    task:Listener timer = createTimer(configurations);
    var result = attachTimer(timer, timerService);
    result = startTimer(timer);
    result = timer.pause();
    if (result is error) {
        return;
    } else {
        isPaused = true;
    }
    result = timer.resume();
    if (result is error) {
        return;
    } else {
        isResumed = true;
    }
}

function getConfigurations(int interval, int delay, int noOfRecurrences)
             returns task:TimerConfiguration {
    task:TimerConfiguration configuration = {
        interval: interval,
        delay: delay,
        noOfRecurrences: noOfRecurrences
    };

    return configuration;
}

function getConfigurationsWithoutRecurrences(int interval, int delay) returns task:TimerConfiguration {
    task:TimerConfiguration configuration = {
        interval: interval,
        delay: delay
    };

    return configuration;
}

function getConfigurationsWithoutDelay(int interval, int noOfRecurrences) returns task:TimerConfiguration {
    task:TimerConfiguration configuration = {
        interval: interval,
        noOfRecurrences: noOfRecurrences
    };

    return configuration;
}

function getConfigurationsWithoutDelayAndRecurrences(int interval) returns task:TimerConfiguration {
    task:TimerConfiguration configuration = {
        interval: interval
    };

    return configuration;
}

function createTimer(task:TimerConfiguration configurations) returns task:Listener {
    task:Listener timer = new(configurations);
    return timer;
}

function attachTimer(task:Listener timer, service s) returns error? {
    return timer.attach(s);
}

function startTimer(task:Listener timer) returns error? {
    return timer.start();
}

function pauseTimer(task:Listener timer) returns error? {
    return timer.pause();
}

function resumeTimer(task:Listener timer) returns error? {
    return timer.resume();
}

function getIsPaused() returns boolean {
    return isPaused;
}

function getIsResumed() returns boolean {
    return isResumed;
}
