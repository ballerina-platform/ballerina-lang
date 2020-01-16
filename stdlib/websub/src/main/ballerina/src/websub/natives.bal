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
import ballerina/io;

import ballerinax/java;

///////////////////////////////////////////////////////////////////
//////////////////// WebSub Hub Natives ///////////////////////////
///////////////////////////////////////////////////////////////////
# Starts up the internal Ballerina Hub.
#
# + basePath - The base path of the hub service
# + subscriptionResourcePath - The resource path for subscription
# + publishResourcePath - The resource path for publishing and topic registration
# + topicRegistrationRequired - Whether a topic needs to be registered at the hub prior to publishing/subscribing
#                               to the topic
# + publicUrl - The URL for the hub to be included in content delivery requests, defaults to
#               `http(s)://localhost:{port}/websub/hub` if unspecified
# + hubListener - The `http:Listener` to which the hub service is attached
# + return - `Hub` The WebSub Hub object representing the newly started up hub, or `HubStartedUpError` indicating
#            that the hub is already started, and including the WebSub Hub object representing the
#            already started up hub
function startUpHubService(string basePath, string subscriptionResourcePath, string publishResourcePath,
                           boolean topicRegistrationRequired, string publicUrl, http:Listener hubListener)
                                    returns Hub|HubStartedUpError|HubStartupError {
    return externStartUpHubService(java:fromString(basePath), java:fromString(subscriptionResourcePath),
                                   java:fromString(publishResourcePath), topicRegistrationRequired,
                                   java:fromString(publicUrl), hubListener);
}

function externStartUpHubService(handle basePath, handle subscriptionResourcePath, handle publishResourcePath,
                                 boolean topicRegistrationRequired, handle publicUrl, http:Listener hubListener)
                                    returns Hub|HubStartedUpError|HubStartupError = @java:Method {
    name: "startUpHubService",
    class: "org.ballerinalang.net.websub.nativeimpl.HubNativeOperationHandler"
} external;

# Stop the Ballerina Hub, if started.
#
# + hub - The `websub:Hub` object returned when starting the hub
# + return - `()` if the Ballerina Hub had been started up and was stopped now, `error` if not
function stopHubService(Hub hub) returns error? = @java:Method {
    class: "org.ballerinalang.net.websub.nativeimpl.HubNativeOperationHandler"
} external;

# Adds a new subscription for the specified topic in the Ballerina Hub.
#
# + subscriptionDetails - The details of the subscription including WebSub specifics
function addSubscription(SubscriptionDetails subscriptionDetails) = @java:Method {
    class: "org.ballerinalang.net.websub.nativeimpl.HubNativeOperationHandler"
} external;

# Publishes an update against the topic in the Ballerina Hub.
#
# + topic - The topic for which the update should happen
# + content - The content to send to subscribers, with the payload and content-type specified
# + return - `error` if an error occurred during publishing
function publishToInternalHub(string topic, WebSubContent content) returns error? {
    return externPublishToInternalHub(java:fromString(topic), content);
}

function externPublishToInternalHub(handle topic, WebSubContent content) returns error? = @java:Method {
    name: "publishToInternalHub",
    class: "org.ballerinalang.net.websub.nativeimpl.HubNativeOperationHandler"
} external;

# Removes a subscription added for the specified topic in the Ballerina Hub.
#
# + topic - The topic for which the subscription was added
# + callback - The callback registered for this subscription
function removeNativeSubscription(string topic, string callback) {
    return externRemoveNativeSubscription(java:fromString(topic), java:fromString(callback));
}

function externRemoveNativeSubscription(handle topic, handle callback) = @java:Method {
    name: "removeNativeSubscription",
    class: "org.ballerinalang.net.websub.nativeimpl.HubNativeOperationHandler"
} external;

# Registers a topic in the Ballerina Hub.
#
# + topic - The topic to register
# + return - `error` if an error occurred with registration
function registerTopicAtNativeHub(string topic) returns error? {
   return externRegisterTopicAtNativeHub(java:fromString(topic));
}

function externRegisterTopicAtNativeHub(handle topic) returns error? = @java:Method {
   name: "registerTopicAtNativeHub",
   class: "org.ballerinalang.net.websub.nativeimpl.HubNativeOperationHandler"
} external;

# Unregisters a topic in the Ballerina Hub.
#
# + topic - The topic to unregister
# + return - `error` if an error occurred with unregistration
function unregisterTopicAtNativeHub(string topic) returns error? {
    return externUnregisterTopicAtNativeHub(java:fromString(topic));
}

function externUnregisterTopicAtNativeHub(handle topic) returns error? = @java:Method {
    name: "unregisterTopicAtNativeHub",
    class: "org.ballerinalang.net.websub.nativeimpl.HubNativeOperationHandler"
} external;

# Retrieves whether a topic is registered with the Ballerina Hub.
#
# + topic - The topic to check
# + return - `boolean` True if the topic has been registered by a publisher, false if not
function isTopicRegistered(string topic) returns boolean {
    return externIsTopicRegistered(java:fromString(topic));
}

function externIsTopicRegistered(handle topic) returns boolean = @java:Method {
    name: "isTopicRegistered",
    class: "org.ballerinalang.net.websub.nativeimpl.HubNativeOperationHandler"
} external;

///////////////////////////////////////////////////////////////////
//////////////////// WebSub Publisher Natives /////////////////////
///////////////////////////////////////////////////////////////////
# Publishes an update against the topic in the Ballerina Hub.
#
# + publishUrl - The publisher URL of the Ballerina WebSub Hub as included in the `websub:Hub` object
# + topic - The topic for which the update should happen
# + content - The content to send to subscribers, with the payload and content-type specified
# + return - `error` if an error occurred during publishing
function validateAndPublishToInternalHub(string publishUrl, string topic, WebSubContent content) returns error? {
    return externValidateAndPublishToInternalHub(java:fromString(publishUrl), java:fromString(topic), content);
}

function externValidateAndPublishToInternalHub(handle publishUrl, handle topic, WebSubContent content)
                                                returns error? = @java:Method {
    name: "validateAndPublishToInternalHub",
    class: "org.ballerinalang.net.websub.nativeimpl.PublisherNativeOperationHandler"
} external;

function constructByteArray(io:ReadableByteChannel byteChannel) returns byte[] = @java:Method {
    class: "org.ballerinalang.net.websub.nativeimpl.PublisherNativeOperationHandler"
} external;
