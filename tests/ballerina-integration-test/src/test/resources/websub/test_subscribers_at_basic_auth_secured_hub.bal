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

import ballerina/auth;
import ballerina/http;
import ballerina/io;
import ballerina/mime;
import ballerina/websub;

listener websub:Listener websubEP = new websub:Listener(8484);

auth:OutboundBasicAuthProvider basicAuthProvider1 = new({ username: "tom", password: "1234" });
http:BasicAuthHandler basicAuthHandler1 = new(basicAuthProvider1);

@websub:SubscriberServiceConfig {
    path: "/websub",
    subscribeOnStartUp: true,
    resourceUrl: "http://localhost:8080/publisher/discover",
    leaseSeconds: 3600,
    secret: "Kslk30SNF2AChs2",
    subscriptionClientConfig: {
        auth: {
            authHandler: basicAuthHandler1
        }
    }
}
service websubSubscriber on websubEP {
    resource function onNotification (websub:Notification notification) returns error? {
        json payload = check notification.getJsonPayload();
        io:println("WebSub Notification Received by One: " + payload.toString());
    }
}

auth:OutboundBasicAuthProvider basicAuthProvider2 = new({ username: "tom", password: "4321" });
http:BasicAuthHandler basicAuthHandler2 = new(basicAuthProvider2);

@websub:SubscriberServiceConfig {
    path: "/websubTwo",
    subscribeOnStartUp: true,
    resourceUrl: "http://localhost:8080/publisherTwo/discover",
    leaseSeconds: 1200,
    subscriptionClientConfig: {
        auth: {
            authHandler: basicAuthHandler2
        }
    }
}
service websubSubscriberTwo on websubEP {
    resource function onNotification (websub:Notification notification) returns error? {
        json payload = check notification.getJsonPayload();
        io:println("WebSub Notification Received by Two: " + payload.toString());
    }
}

auth:OutboundBasicAuthProvider basicAuthProvider3 = new({ username: "mary", password: "xyz" });
http:BasicAuthHandler basicAuthHandler3 = new(basicAuthProvider3);

@websub:SubscriberServiceConfig {
    path: "/websubThree",
    subscribeOnStartUp: true,
    resourceUrl: "http://localhost:8080/publisher/discover",
    leaseSeconds: 1200,
    subscriptionClientConfig: {
        auth: {
            authHandler: basicAuthHandler3
        }
    }
}
service websubSubscriberThree on websubEP {
    resource function onNotification (websub:Notification notification) returns error? {
        json payload = check notification.getJsonPayload();
        io:println("WebSub Notification Received by Three: " + payload.toString());
    }
}

auth:OutboundBasicAuthProvider basicAuthProvider4 = new({ username: "tom", password: "1234" });
http:BasicAuthHandler basicAuthHandler4 = new(basicAuthProvider4);

@websub:SubscriberServiceConfig {
    path: "/websubFour",
    subscribeOnStartUp: true,
    resourceUrl: "http://localhost:8080/publisherThree/discover",
    leaseSeconds: 1200,
    subscriptionClientConfig: {
        auth: {
            authHandler: basicAuthHandler4
        }
    }
}
service websubSubscriberFour on websubEP {
    resource function onNotification (websub:Notification notification) returns error? {
        json payload = check notification.getJsonPayload();
        io:println("WebSub Notification Received by Four: " + payload.toString());
    }
}
