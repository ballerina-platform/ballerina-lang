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

import ballerina/config;
import ballerina/io;
import ballerina/http;
import ballerina/websub;

listener websub:Listener websubEP = new websub:Listener(23181, { host: "0.0.0.0" });

@websub:SubscriberServiceConfig {
    path:"/websub",
    subscribeOnStartUp:true,
    target: [config:getAsString("test.hub.url"), "http://one.websub.topic.com"]
}
service websubSubscriber on websubEP {
    resource function onNotification (websub:Notification notification) {
        json payload = <json> notification.getJsonPayload();
        io:println("WebSub Notification Received: ", payload.toJsonString());
    }
}

@websub:SubscriberServiceConfig {
    target: [config:getAsString("test.hub.url"), "http://one.websub.topic.com"],
    leaseSeconds: 3650,
    secret: "Kslk30SNF2AChs2"
}
service subscriberWithNoPathInAnnot on websubEP {
    resource function onIntentVerification (websub:Caller caller, websub:IntentVerificationRequest request) {
        http:Response response = request.buildSubscriptionVerificationResponse("http://one.websub.topic.com");
        if (response.statusCode == 202) {
            io:println("Intent verified explicitly for subscription change request");
        } else {
            io:println("Intent verification denied explicitly for subscription change request");
        }
        var result = caller->respond(<@untainted> response);
        if (result is error) {
            io:println("Error responding to intent verification request: ", result);
        }
    }

    resource function onNotification (websub:Notification notification) {
        string payload = <string> notification.getTextPayload();
        io:println("WebSub Notification Received by Two: ", payload);
    }
}

string subscriberThreeTopic = "http://one.websub.topic.com";

@websub:SubscriberServiceConfig {
    path:"/websubThree",
    subscribeOnStartUp:true,
    target: [config:getAsString("test.hub.url"), subscriberThreeTopic],
    leaseSeconds: 300,
    callback: "http://localhost:23181/websubThree?topic=" + subscriberThreeTopic + "&fooVal=barVal",
    secret: "Xaskdnfe234"
}
service websubSubscriberWithQueryParams on websubEP {
    resource function onNotification (websub:Notification notification) {
        string payload = <string> notification.getTextPayload();
        io:println("WebSub Notification Received by Three: ", payload);
        io:println("Query Params: ", notification.getQueryParams());
    }
}
