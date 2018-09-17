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

///////////////////////////////////////////////////////////////////
//////////////////// WebSub Hub Natives ///////////////////////////
///////////////////////////////////////////////////////////////////
# Starts up the internal Ballerina Hub.
#
# + topicRegistrationRequired - Whether a topic needs to be registered at the hub prior to publishing/subscribing
#                               to the topic
# + publicUrl - The URL for the hub to be included in content delivery requests, defaults to
#               `http(s)://localhost:{port}/websub/hub` if unspecified
# + return - `WebSubHub` The WebSubHub object representing the newly started up hub, or `HubStartedUpError` indicating
#            that the hub is already started, and including the WebSubHub object representing the
#            already started up hub
extern function startUpHubService(boolean topicRegistrationRequired, string publicUrl)
                                                                            returns WebSubHub|HubStartedUpError;

# Stop the Ballerina Hub, if started.
#
# + hubUrl - The URL of the Hub service
# + return - `boolean` True if the Ballerina Hub had been started up and was stopped now, false if the Hub had not been
#            started up
extern function stopHubService(string hubUrl) returns boolean;

# Adds a new subscription for the specified topic in the Ballerina Hub.
#
# + subscriptionDetails - The details of the subscription including WebSub specifics
extern function addSubscription(SubscriptionDetails subscriptionDetails);

# Publishes an update against the topic in the Ballerina Hub.
#
# + topic - The topic for which the update should happen
# + content - The content to send to subscribers, with the payload and content-type specified
# + return - `error` if an error occurred during publishing
extern function publishToInternalHub(string topic, WebSubContent content) returns error?;

# Removes a subscription added for the specified topic in the Ballerina Hub.
#
# + topic - The topic for which the subscription was added
# + callback - The callback registered for this subscription
extern function removeSubscription(string topic, string callback);

# Registers a topic in the Ballerina Hub.
#
# + topic - The topic to register
# + secret - The secret to use to identify the registration
# + loadingOnStartUp - Whether registration is being called on loading from the database at start up
# + return - `error` if an error occurred with registration
extern function registerTopicAtHub(string topic, string secret, boolean loadingOnStartUp = false) returns error?;

# Unregisters a topic in the Ballerina Hub.
#
# + topic - The topic to unregister
# + secret - The secret specified at registration
# + return - `error` if an error occurred with unregistration
extern function unregisterTopicAtHub(string topic, string secret) returns error?;

# Retrieves whether a topic is registered with the Ballerina Hub.
#
# + topic - The topic to check
# + return - `boolean` True if the topic has been registered by a publisher, false if not
extern function isTopicRegistered(string topic) returns boolean;

# Retrieves secret for a topic registered with the Ballerina Hub.
#
# + topic - The topic for which the publisher's secret needs to be retrieved
# + return - `string` The secret specified at registration
extern function retrievePublisherSecret(string topic) returns string;

///////////////////////////////////////////////////////////////////
//////////////////// WebSub Publisher Natives /////////////////////
///////////////////////////////////////////////////////////////////
# Publishes an update against the topic in the Ballerina Hub.
#
# + hubUrl - The URL of the Ballerina WebSub Hub as included in the WebSubHub struct
# + topic - The topic for which the update should happen
# + content - The content to send to subscribers, with the payload and content-type specified
# + return - `error` if an error occurred during publishing
extern function validateAndPublishToInternalHub(string hubUrl, string topic, WebSubContent content) returns error?;

extern function constructByteArray(io:ByteChannel byteChannel) returns byte[];
