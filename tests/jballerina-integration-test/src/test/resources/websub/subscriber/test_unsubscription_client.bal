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

import ballerina/io;
import ballerina/runtime;
import ballerina/websub;

// This is the client used to send subscription and unsubscription requests.
websub:SubscriptionClient websubHubClientEP = new ("http://localhost:23191/websub/hub");

public function main(string... args) {

    // Send unsubscription request for the subscriber service.
    websub:SubscriptionChangeRequest unsubscriptionRequest = {
        topic: "http://one.websub.topic.com",
        callback: "http://localhost:23181/websub"
    };

    var response = websubHubClientEP->unsubscribe(unsubscriptionRequest);
    if (response is websub:SubscriptionChangeResponse) {
        io:println("Unsubscription Request successful at Hub [" + response.hub + "] for Topic [" +
                        response.topic + "]");
    }
    else {
        error err = response;
        string errCause = <string> err.detail()["message"];
        io:println("Error occurred with Unsubscription Request: ", errCause);
    }

    // Confirm unsubscription - no notifications should be received.
    runtime:sleep(5000);
}
