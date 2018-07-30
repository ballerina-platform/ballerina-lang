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
documentation {
    Starts up the internal Ballerina Hub.

    P{{topicRegistrationRequired}}  Whether a topic needs to be registered at the hub prior to publishing/subscribing
                                        to the topic
    P{{publicUrl}}                  The URL for the hub to be included in content delivery requests, defaults to
                                        `http(s)://localhost:{port}/websub/hub` if unspecified
    R{{}} `WebSubHub` The WebSubHub object representing the newly started up hub, or `HubStartedUpError` indicating
                        that the hub is already started, and including the WebSubHub object representing the
                        already started up hub
}
extern function startUpHubService(boolean topicRegistrationRequired, string publicUrl)
                                                                            returns WebSubHub|HubStartedUpError;

documentation {
    Stop the Ballerina Hub, if started.
    
    P{{hubUrl}} The URL of the Hub service
    R{{}} `boolean` True if the Ballerina Hub had been started up and was stopped now, false if the Hub had not been
          started up
}
extern function stopHubService(string hubUrl) returns boolean;

documentation {
    Adds a new subscription for the specified topic in the Ballerina Hub.

    P{{subscriptionDetails}} The details of the subscription including WebSub specifics
}
extern function addSubscription(SubscriptionDetails subscriptionDetails);

documentation {
    Publishes an update against the topic in the Ballerina Hub.

    P{{topic}} The topic for which the update should happen
    P{{content}} The content to send to subscribers, with the payload and content-type specified
    R{{}} `error` if an error occurred during publishing
}
extern function publishToInternalHub(string topic, WebSubContent content) returns error?;

documentation {
    Removes a subscription added for the specified topic in the Ballerina Hub.

    P{{topic}} The topic for which the subscription was added
    P{{callback}} The callback registered for this subscription
}
extern function removeSubscription(string topic, string callback);

documentation {
    Registers a topic in the Ballerina Hub.

    P{{topic}} The topic to register
    P{{secret}} The secret to use to identify the registration
    P{{loadingOnStartUp}} Whether registration is being called on loading from the database at start up
    R{{}} `error` if an error occurred with registration
}
extern function registerTopicAtHub(string topic, string secret, boolean loadingOnStartUp = false) returns error?;

documentation {
    Unregisters a topic in the Ballerina Hub.

    P{{topic}} The topic to unregister
    P{{secret}} The secret specified at registration
    R{{}} `error` if an error occurred with unregistration
}
extern function unregisterTopicAtHub(string topic, string secret) returns error?;

documentation {
    Retrieves whether a topic is registered with the Ballerina Hub.

    P{{topic}} The topic to check
    R{{}} `boolean` True if the topic has been registered by a publisher, false if not
}
extern function isTopicRegistered(string topic) returns boolean;

documentation {
    Retrieves secret for a topic registered with the Ballerina Hub.

    P{{topic}} The topic for which the publisher's secret needs to be retrieved
    R{{}} `string` The secret specified at registration
}
extern function retrievePublisherSecret(string topic) returns string;

///////////////////////////////////////////////////////////////////
//////////////////// WebSub Publisher Natives /////////////////////
///////////////////////////////////////////////////////////////////
documentation {
    Publishes an update against the topic in the Ballerina Hub.

    P{{hubUrl}} The URL of the Ballerina WebSub Hub as included in the WebSubHub struct
    P{{topic}} The topic for which the update should happen
    P{{content}} The content to send to subscribers, with the payload and content-type specified
    R{{}} `error` if an error occurred during publishing
}
extern function validateAndPublishToInternalHub(string hubUrl, string topic, WebSubContent content) returns error?;

extern function constructByteArray(io:ByteChannel byteChannel) returns byte[];
