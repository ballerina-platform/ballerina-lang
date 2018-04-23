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

///////////////////////////////////////////////////////////////////
//////////////////// WebSub Subscriber Natives ////////////////////
///////////////////////////////////////////////////////////////////
documentation {
    Function to retrieve annotations specified for the WebSub Subscriber Service.
    
    R{{}} `SubscriberServiceConfiguration` representing the annotation
}
native function retrieveAnnotations() returns SubscriberServiceConfiguration;

documentation {
    Function to retrieve annotations specified for the WebSub Subscriber Service.

    P{{serviceType}} the typedesc for the service
    R{{}} `string` The secret specified in the the annotation
}
native function retrieveSecret(typedesc serviceType) returns string;

///////////////////////////////////////////////////////////////////
//////////////////// WebSub Hub Natives ///////////////////////////
///////////////////////////////////////////////////////////////////
documentation {
    Starts up the internal Ballerina Hub.
    
    R{{}} `string` The URL of the Hub service
}
native function startUpHubService() returns string;

documentation {
    Stop the Ballerina Hub, if started.
    
    P{{hubUrl}} The URL of the Hub service
    R{{}} `boolean` True if the Ballerina Hub had been started up and was stopped now, false if the Hub had not been
          started up
}
native function stopHubService(string hubUrl) returns boolean;

//TODO: move the following functions to the net.websub.hub package and make private once packaging issue is sorted
documentation {
    Adds a new subscription for the specified topic in the Ballerina Hub.

    P{{subscriptionDetails}} The details of the subscription including WebSub specifics
}
public native function addSubscription(SubscriptionDetails subscriptionDetails);

documentation {
    Publishes an update against the topic in the Ballerina Hub.

    P{{topic}} The topic for which the update should happen
    P{{payload}} The update payload
    R{{}} `string` Error Message if an error occurred with publishing
}
public native function publishToInternalHub(string topic, json payload) returns string;

documentation {
    Removes a subscription added for the specified topic in the Ballerina Hub.

    P{{topic}} The topic for which the subscription was added
    P{{callback}} The callback registered for this subscription
}
public native function removeSubscription(string topic, string callback);

documentation {
    Registers a topic in the Ballerina Hub.

    P{{topic}} The topic to register
    P{{secret}} The secret to use to identify the registration
    P{{loadingOnStartUp}} Whether registration is being called on loading from the database at start up
    R{{}} `string` Error Message if an error occurred with registration
}
public native function registerTopicAtHub(string topic, string secret, boolean loadingOnStartUp = false) returns string;

documentation {
    Unregisters a topic in the Ballerina Hub.

    P{{topic}} The topic to unregister
    P{{secret}} The secret specified at registration
    R{{}} `string` Error Message if an error occurred with unregistration
}
public native function unregisterTopicAtHub(string topic, string secret) returns string;

documentation {
    Retrieves whether a topic is registered with the Ballerina Hub.

    P{{topic}} The topic to check
    R{{}} `boolean` True if the topic has been registered by a publisher, false if not
}
public native function isTopicRegistered(string topic) returns boolean;

documentation {
    Retrieves secret for a topic registered with the Ballerina Hub.

    P{{topic}} The topic for which the publisher's secret needs to be retrieved
    R{{}} `string` The secret specified at registration
}
public native function retrievePublisherSecret(string topic) returns string;

///////////////////////////////////////////////////////////////////
//////////////////// WebSub Publisher Natives /////////////////////
///////////////////////////////////////////////////////////////////
documentation {
    Publishes an update against the topic in the Ballerina Hub.

    P{{hubUrl}} The URL of the Ballerina WebSub Hub as included in the WebSubHub struct
    P{{topic}} The topic for which the update should happen
    P{{payload}} The update payload
    R{{}} `string` String indicating the error, if an error occurred
}
native function validateAndPublishToInternalHub(string hubUrl, string topic, json payload) returns string;
