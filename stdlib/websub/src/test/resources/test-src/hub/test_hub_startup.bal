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

import ballerina/websub;
import ballerina/http;

function startupHub(int hubPort) returns websub:Hub|websub:HubStartedUpError|websub:HubStartupError {
    return websub:startHub(new http:Listener(hubPort), "/websub", "/hub");
}

function stopHub(websub:Hub|websub:HubStartedUpError|websub:HubStartupError hubStartUpResult) {
    if (hubStartUpResult is websub:Hub) {
        checkpanic hubStartUpResult.stop();
    } else if (hubStartUpResult is  websub:HubStartedUpError) {
        checkpanic hubStartUpResult.startedUpHub.stop();
    } else {
        panic hubStartUpResult;
    }
}

function testPublisherAndSubscriptionInvalidSameResourcePath() returns boolean {
    http:Listener lis = new (9393);
    websub:Hub|websub:HubStartedUpError|websub:HubStartupError res =
        websub:startHub(lis, "/websub", "/hub", "/hub");

    var err = lis.__immediateStop();

    if (res is websub:HubStartupError) {
        return res.reason() == "{ballerina/websub}HubStartupError" &&
            res.detail().message == "publisher and subscription resource paths cannot be the same";
    }
    return false;
}
