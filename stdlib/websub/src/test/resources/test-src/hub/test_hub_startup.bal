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

import ballerina/http;
import ballerina/websub;

function testHubStartUp() returns boolean {
    http:Listener lis0 = new (9191);
    http:Listener lis1 = new (9292);
    websub:Hub|websub:HubStartedUpError|websub:HubStartupError res =
        websub:startHub(lis0, "/websub", "/hub", "/pub", publicUrl = "https://localhost:9191");

    if (res is websub:Hub) {
        if (res.publishUrl != "https://localhost:9191/websub/pub" ||
                res.subscriptionUrl != "https://localhost:9191/websub/hub") {
            return false;
        }
    } else {
        return false;
    }

    // testHubStartUpWhenStarted
    websub:Hub|websub:HubStartedUpError|websub:HubStartupError res2 =
            websub:startHub(lis1);

    if !(res2 is websub:HubStartedUpError) || res2.startedUpHub !== res {
        return false;
    }

    // testHubShutdownAndStart
    websub:Hub hub = <websub:Hub> res;
    error? err = hub.stop();
    if (err is error) {
        return false;
    }
    err = lis0.__gracefulStop();
    err = lis1.__gracefulStop();

    http:Listener lis2 = new (9393);
    res2 = websub:startHub(lis2);
    err = lis2.__gracefulStop();
    if res2 is websub:Hub {
        boolean b = res2.publishUrl == "http://localhost:9393/publish" &&
            res2.subscriptionUrl == "http://localhost:9393/";
        err = hub.stop();
        return b;
    }
    return false;
}

function testPublisherAndSubscriptionInvalidSameResourcePath() returns boolean {
    http:Listener lis = new (9494);
    websub:Hub|websub:HubStartedUpError|websub:HubStartupError res =
        websub:startHub(lis, "/websub", "/hub", "/hub");

    var err = lis.__gracefulStop();

    if (res is websub:HubStartupError) {
        return res.reason() == "{ballerina/websub}HubStartupError" &&
            res.detail().message == "publisher and subscription resource paths cannot be the same";
    }
    return false;
}
