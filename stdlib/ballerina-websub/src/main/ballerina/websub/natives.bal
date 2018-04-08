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
package ballerina.websub;

///////////////////////////////////////////////////////////////////
//////////////////// WebSub Subscriber Natives ////////////////////
///////////////////////////////////////////////////////////////////
@Description {value:"Function to retrieve annotations specified for the WebSub Subscriber Service"}
@Return {value:"SubscriberServiceConfiguration representing the annotation"}
native function retrieveAnnotations () returns (SubscriberServiceConfiguration);

@Description {value:"Function to retrieve annotations specified for the WebSub Subscriber Service"}
@Return {value:"WebSubSubscriberServiceConfiguration representing the annotation"}
native function retrieveSecret (typedesc serviceType) returns (string);

///////////////////////////////////////////////////////////////////
//////////////////// WebSub Hub Natives ///////////////////////////
///////////////////////////////////////////////////////////////////
@Description {value:"Starts up the internal Ballerina Hub"}
@Return {value:"The URL of the Hub service"}
native function startUpHubService () returns (string);

@Description {value:"Stop the Ballerina Hub, if started"}
@Param {value:"The URL of the Hub service"}
@Return {value:"True if the Ballerina Hub had been started up and was stopped now, false if the Hub had not been started
                up"}
native function stopHubService (string hubUrl) returns (boolean);

//TODO: move the following functions to the net.websub.hub package and make private once packaging issue is sorted
@Description {value:"Adds a new subscription for the specified topic in the Ballerina Hub"}
@Param {value:"subscriptionDetails: The details of the subscription including WebSub specifics"}
public native function addSubscription (SubscriptionDetails subscriptionDetails);

@Description {value:"Publishes an update against the topic in the Ballerina Hub"}
@Param {value:"topic: The topic for which the update should happen"}
@Param {value:"payload: The update payload"}
@Return {value:"Error Message if an error occurred with publishing"}
public native function publishToInternalHub (string topic, json payload) returns (string);

@Description {value:"Removes a subscription added for the specified topic in the Ballerina Hub"}
@Param {value:"topic: The topic for which the subscription was added"}
@Param {value:"callback: The callback registered for this subscription"}
public native function removeSubscription (string topic, string callback);

@Description {value:"Registers a topic in the Ballerina Hub"}
@Param {value:"topic: The topic to register"}
@Param {value:"secret: The secret to use to identify the registration"}
@Param {value:"loadingOnStartUp: Whether registration is being called on loading from the database at start up"}
@Return {value:"Error Message if an error occurred with registration"}
public native function registerTopicAtHub (string topic, string secret, boolean loadingOnStartUp = false) returns
(string);

@Description {value:"Unregisters a topic in the Ballerina Hub"}
@Param {value:"topic: The topic to unregister"}
@Param {value:"secret: The secret specified at registration"}
@Return {value:"Error Message if an error occurred with unregistration"}
public native function unregisterTopicAtHub (string topic, string secret) returns (string);

@Description {value:"Retrieves whether a topic is registered with the Ballerina Hub"}
@Param {value:"topic: The topic to check"}
@Return {value:"True if the topic has been registered by a publisher, false if not"}
public native function isTopicRegistered (string topic) returns (boolean);

@Description {value:"Retrieves secret for a topic registered with the Ballerina Hub"}
@Param {value:"topic: The topic for which the publisher's secret needs to be retrieved"}
@Return {value:"The secret specified at registration"}
public native function retrievePublisherSecret (string topic) returns (string);

///////////////////////////////////////////////////////////////////
//////////////////// WebSub Publisher Natives /////////////////////
///////////////////////////////////////////////////////////////////
@Description {value:"Publishes an update against the topic in the Ballerina Hub"}
@Param {value:"hubUrl: The URL of the Ballerina WebSub Hub as included in the WebSubHub struct"}
@Param {value:"topic: The topic for which the update should happen"}
@Param {value:"payload: The update payload"}
@Return {value:"String indicating the error, if an error occurred"}
native function validateAndPublishToInternalHub (string hubUrl, string topic, json payload) returns (string);
