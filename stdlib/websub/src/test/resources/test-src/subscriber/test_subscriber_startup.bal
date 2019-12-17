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

import ballerina/log;
import ballerina/websub;

websub:Listener l1 = new(8387);
websub:Listener l2 = new(8387);

function startSubscriberService() returns string {
    var l1Error = l1.__start();
    if (l1Error is error) {
        log:printError("listener_1 has not started");
        return l1Error.detail()?.message ?: "l1 error unavailable";
    } else {
        log:printInfo("listener_1 has started");
    }

    var l2Error = l2.__start();
    if (l2Error is error) {
        log:printError("listener_2 has not started");
        return <string>l2Error.detail()?.message;
    } else {
        log:printInfo("listener_2 has started");
    }
    return "no error";
}

function stopSubscriberService() returns string {
    var l1Error = l1.__gracefulStop();
    if (l1Error is error) {
        log:printError("listener_1 has not stopped");
        return <string>l1Error.detail()?.message;
    }
    log:printError("listener_1 has stopped");
    return "Successfully stopped";
}
