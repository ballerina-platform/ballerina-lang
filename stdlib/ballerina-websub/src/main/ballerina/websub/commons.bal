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

import ballerina/http;
import ballerina/log;
import ballerina/mime;
import ballerina/security.crypto;

@final public string HUB_CHALLENGE = "hub.challenge";
@final public string HUB_MODE = "hub.mode";
@final public string HUB_TOPIC = "hub.topic";
@final public string HUB_CALLBACK = "hub.callback";
@final public string HUB_LEASE_SECONDS = "hub.lease_seconds";
@final public string HUB_SECRET = "hub.secret";

@final public string MODE_SUBSCRIBE = "subscribe";
@final public string MODE_UNSUBSCRIBE = "unsubscribe";
@final public string MODE_PUBLISH = "publish";
@final public string MODE_REGISTER = "register";
@final public string MODE_UNREGISTER = "unregister";
@final public string PUBLISHER_SECRET = "publisher.secret";
@final public string REMOTE_PUBLISHING_MODE_DIRECT = "direct";
@final public string REMOTE_PUBLISHING_MODE_FETCH = "fetch";

@final public string X_HUB_UUID = "X-Hub-Uuid";
@final public string X_HUB_TOPIC = "X-Hub-Topic";
@final public string X_HUB_SIGNATURE = "X-Hub-Signature";
@final public string PUBLISHER_SIGNATURE = "Publisher-Signature";

@final public string CONTENT_TYPE = "Content-Type";
@final public string SHA1 = "SHA1";
@final public string SHA256 = "SHA256";
@final public string MD5 = "MD5";

public type TopicIdentifier "TOPIC_ID_HEADER" | "TOPIC_ID_PAYLOAD_KEY" | "TOPIC_ID_HEADER_AND_PAYLOAD";
@final public TopicIdentifier TOPIC_ID_HEADER = "TOPIC_ID_HEADER";
@final public TopicIdentifier TOPIC_ID_PAYLOAD_KEY = "TOPIC_ID_PAYLOAD_KEY";
@final public TopicIdentifier TOPIC_ID_HEADER_AND_PAYLOAD = "TOPIC_ID_HEADER_AND_PAYLOAD";

@Description {value:"Struct to represent WebSub related errors"}
@Field {value:"errorMessage: Error message indicating an issue"}
@Field {value:"connectorError: HttpConnectorError if occurred"}
public type WebSubError {
    string message,
    error? cause,
};

///////////////////////////////////////////////////////////////////
//////////////////// WebSub Subscriber Commons ////////////////////
///////////////////////////////////////////////////////////////////
@Description {value:"Object representing and intent verification request received"}
@Field {value:"mode: The mode specified whether intent is being verified for subscription or unsubscription"}
@Field {value:"topic: The for which intent is being verified for subscription or unsubscription"}
@Field {value:"challenge: The challenge to be echoed to verify intent to subscribe/unsubscribe"}
@Field {value:"leaseSeconds: The lease seconds period for which a subscription will be active if intent verification
is being done for subscription"}
@Field {value:"request: The HTTP request received for intent verification"}
public type IntentVerificationRequest object {

    public {
        string mode;
        string topic;
        string challenge;
        int leaseSeconds;
        http:Request request;
    }

    @Description {value:"Function to build intent verification response for subscription requests sent"}
    @Param {value:"topic: The topic for which subscription should be accepted if not annotated"}
    @Return {value:"The response to the hub verifying/denying intent to subscribe"}
    public function buildSubscriptionVerificationResponse (string topic = "") returns (http:Response | ());

    @Description {value:"Function to build intent verification response for unsubscription requests sent"}
    @Param {value:"topic: The topic for which unsubscription should be accepted if not annotated"}
    @Return {value:"The response to the hub verifying/denying intent to subscribe"}
    public function buildUnsubscriptionVerificationResponse (string topic = "") returns (http:Response | ());

};

public function IntentVerificationRequest::buildSubscriptionVerificationResponse (string topic = "") returns
(http:Response | ()) {
    SubscriberServiceConfiguration subscriberServiceConfiguration = {};
    if (topic == "") {
        subscriberServiceConfiguration = retrieveAnnotations();
    } else {
        subscriberServiceConfiguration = { topic:topic };
    }
    return buildIntentVerificationResponse(self, MODE_SUBSCRIBE, subscriberServiceConfiguration);
}

public function IntentVerificationRequest::buildUnsubscriptionVerificationResponse (string topic = "") returns
(http:Response | ()) {
    SubscriberServiceConfiguration subscriberServiceConfiguration = {};
    if (topic == "") {
        subscriberServiceConfiguration = retrieveAnnotations();
    } else {
        subscriberServiceConfiguration = { topic:topic };
    }
    return buildIntentVerificationResponse(self, MODE_UNSUBSCRIBE, subscriberServiceConfiguration);
}

@Description { value : "Function to build intent verification response for subscription/unsubscription requests sent" }
@Param { value : "request: The intent verification request from the hub" }
@Param { value : "mode: The mode (subscription/unsubscription) for which a request was sent" }
@Return { value : "The response to the hub verifying/denying intent to subscripe/unsubscribe" }
function buildIntentVerificationResponse(IntentVerificationRequest intentVerificationRequest, string mode,
                        SubscriberServiceConfiguration webSubSubscriberAnnotations) returns (http:Response | ()) {
    http:Response response = new;
    string topic = webSubSubscriberAnnotations.topic;
    if (topic == "") {
        log:printError("Unable to verify intent since the topic is not specified");
        return;
    }

    string reqMode = intentVerificationRequest.mode;
    string challenge = intentVerificationRequest.challenge;
    string reqTopic = intentVerificationRequest.topic;

    match (http:decode(reqTopic, "UTF-8")) {
        string decodedTopic => reqTopic = decodedTopic;
        error => {}
    }

    string reqLeaseSeconds = <string> intentVerificationRequest.leaseSeconds;

    if (reqMode == mode && reqTopic == topic) {
        response.statusCode = http:ACCEPTED_202;
        response.setStringPayload(challenge);
        log:printInfo("Intent Verification agreed - Mode [" + mode + "], Topic [" + topic +"], Lease Seconds ["
                      + reqLeaseSeconds + "]");
    } else {
        response.statusCode = http:NOT_FOUND_404;
        log:printWarn("Intent Verification denied - Mode [" + mode + "], Topic [" + topic +"]");
    }
    return response;

}

@Description {value:"Function to validate signature for requests received at the callback" }
@Param {value:"request: The request received"}
@Param {value:"serviceType: The type of the service for which the request was rceived"}
@Return {value:"WebSubError, if an error occurred in extraction or signature validation failed"}
public function processWebSubNotification(http:Request request, typedesc serviceType) returns (WebSubError | ()) {
    string secret = retrieveSecret(serviceType);
    string xHubSignature;

    if (request.hasHeader(X_HUB_SIGNATURE)) {
        xHubSignature = request.getHeader(X_HUB_SIGNATURE);
    } else {
        if (secret != "") {
            WebSubError webSubError = { message:X_HUB_SIGNATURE + " header not present for subscription added" +
                                      " specifying " + HUB_SECRET };
            return webSubError;
        } else {
            return;
        }
    }

    json payload;
    var reqJsonPayload = request.getJsonPayload(); //TODO: fix for all types
    match (reqJsonPayload) {
        json jsonPayload => { payload = jsonPayload; }
        mime:EntityError entityError => {
            WebSubError webSubError = { message:"Error extracting notification payload", cause: entityError };
            return webSubError;
        }
    }

    if (secret == "" && xHubSignature != "") {
        log:printWarn("Ignoring " + X_HUB_SIGNATURE + " value since secret is not specified.");
        return;
    } else {
        string strPayload = payload.toString() but { () => "" };
        return validateSignature(xHubSignature, strPayload, secret);
    }
}

@Description {value:"Function to validate the signature header included in the notification"}
@Param {value:"payload: The string representation of the notification payload received"}
@Param {value:"secret: The secret used when subscribing"}
@Return {value:"WebSubError if an error occurs validating the signature or the signature is invalid"}
public function validateSignature (string xHubSignature, string stringPayload, string secret) returns
(WebSubError | ()) {
    string[] splitSignature = xHubSignature.split("=");
    string method = splitSignature[0];
    string signature = xHubSignature.replace(method + "=", "");
    string generatedSignature;

    if (SHA1.equalsIgnoreCase(method)) {
        generatedSignature = crypto:getHmac(stringPayload, secret, crypto:SHA1);
    } else if (SHA256.equalsIgnoreCase(method)) {
        generatedSignature = crypto:getHmac(stringPayload, secret, crypto:SHA256);
    } else if (MD5.equalsIgnoreCase(method)) {
        generatedSignature = crypto:getHmac(stringPayload, secret, crypto:MD5);
    } else {
        WebSubError webSubError = { message:"Unsupported signature method: " + method };
        return webSubError;
    }

    if (!signature.equalsIgnoreCase(generatedSignature)) {
        WebSubError webSubError = { message:"Signature validation failed: Invalid Signature!" };
        return webSubError;
    }
    return;
}

@Description {value:"Record representing the WebSubSubscriber notification received"}
@Field {value:"payload: The payload of the notification received"}
@Field {value:"request: The HTTP POST request received as the notification"}
public type NotificationRequest {
    json payload,
    http:Request request,
};

@Description {value:"Record to represent a WebSub subscription request"}
@Field {value:"topic: The topic for which the subscription/unsubscription request is sent"}
@Field {value:"callback: The callback which should be registered/unregistered for the subscription/unsubscription
                request is sent"}
@Field {value:"leaseSeconds: The lease period for which the subscription is expected to be active"}
@Field {value:"secret: The secret to be used for authenticated content distribution with this subscription"}
public type SubscriptionChangeRequest {
    string topic,
    string callback,
    int leaseSeconds,
    string secret,
};

@Description {value:"Record to represent subscription/unsubscription details on success"}
@Field {value:"hub: The hub at which the subscription/unsubscription was successful"}
@Field {value:"topic: The topic for which the subscription/unsubscription was successful"}
@Field {value:"response: The response from the hub to the subscription/unsubscription requests"}
public type SubscriptionChangeResponse {
    string hub,
    string topic,
    http:Response response,
};

/////////////////////////////////////////////////////////////
//////////////////// WebSub Hub Commons /////////////////////
/////////////////////////////////////////////////////////////
@Description {value:"Starts up the Ballerina Hub"}
@Param {value:"ballerinaWebSubHub: The WebSubHub struct representing the started up hub"}
public function startUpBallerinaHub () returns (WebSubHub) {
    string hubUrl = startUpHubService();
    WebSubHub ballerinaWebSubHub = new WebSubHub(hubUrl);
    return ballerinaWebSubHub;
}

@Description {value:"Struct to represent a WebSub Hub"}
@Field {value:"hubUrl: The URL of the WebSub Hub"}
public type WebSubHub object {

    public {
        string hubUrl;
    }

    new (hubUrl) {}

    @Description {value:"Stops the started up Ballerina Hub"}
    @Return {value:"Boolean indicating whether the internal Ballerina Hub was stopped"}
    public function stop () returns (boolean);

    @Description {value:"Publishes an update against the topic in the initialized Ballerina Hub"}
    @Param {value:"topic: The topic for which the update should happen"}
    @Param {value:"payload: The update payload"}
    @Return {value:"WebSubError if the hub is not initialized or does not represent the internal hub"}
    public function publishUpdate (string topic, json payload) returns (WebSubError | ());

    @Description {value:"Registers a topic in the Ballerina Hub"}
    @Param {value:"topic: The topic to register"}
    @Return {value:"WebSubError if an error occurred with registration"}
    public function registerTopic (string topic) returns (WebSubError | ());

    @Description {value:"Unregisters a topic in the Ballerina Hub"}
    @Param {value:"topic: The topic to unregister"}
    @Return {value:"WebSubError if an error occurred with unregistration"}
    public function unregisterTopic (string topic) returns (WebSubError | ());

};

public function WebSubHub::stop () returns (boolean) {
    //TODO: fix to stop
    return stopHubService(self.hubUrl);
}

public function WebSubHub::publishUpdate (string topic, json payload) returns (WebSubError | ()) {
    if (self.hubUrl == "") {
        WebSubError webSubError = { message:"Internal Ballerina Hub not initialized or incorrectly referenced" };
        return webSubError;
    } else {
        string errorMessage = validateAndPublishToInternalHub(self.hubUrl, topic, payload);
        if (errorMessage != "") {
            WebSubError webSubError = { message:errorMessage };
            return webSubError;
        }
    }
    return;
}

public function WebSubHub::registerTopic (string topic) returns (WebSubError | ()) {
    string errorMessage = registerTopicAtHub(topic, "");
    if (errorMessage != "") {
        WebSubError webSubError = { message:errorMessage };
        return webSubError;
    }
    return;
}

public function WebSubHub::unregisterTopic (string topic) returns (WebSubError | ()) {
    string errorMessage = unregisterTopicAtHub(topic, "");
    if (errorMessage != "") {
        WebSubError webSubError = { message:errorMessage };
        return webSubError;
    }
    return;
}

///////////////////////////////////////////////////////////////////
//////////////////// WebSub Publisher Commons /////////////////////
///////////////////////////////////////////////////////////////////
@Description {value:"Function to add link headers to a response to allow WebSub discovery"}
@Param {value:"response: The response being sent"}
@Param {value:"hubs: The hubs the publisher advertises as the hubs that it publishes updates to"}
@Param {value:"topic: The topic to which subscribers need to subscribe to, to receive updates for the resource/topic"}
@Return{value:"Response with the link header added"}
public function addWebSubLinkHeaders (http:Response response, string[] hubs, string topic) returns (http:Response) {
    string hubLinkHeader = "";
    foreach hub in hubs {
        hubLinkHeader = hubLinkHeader + "<" + hub + ">; rel=\"hub\", ";
    }
    response.setHeader("Link", hubLinkHeader + "<" + topic + ">; rel=\"self\"");
    return response;
}

@Description {value:"Struct to represent Subscription Details retrieved from the database"}
@Field {value:"topic: The topic for which the subscription is added"}
@Field {value:"callback: The callback specified for the particular subscription"}
@Field {value:"secret: The secret to be used for authenticated content distribution"}
@Field {value:"leaseSeconds: The lease second period specified for the particular subscription"}
@Field {value:"createdAt: The time at which the subscription was created"}
public type SubscriptionDetails {
    string topic,
    string callback,
    string secret,
    int leaseSeconds,
    int createdAt,
};
