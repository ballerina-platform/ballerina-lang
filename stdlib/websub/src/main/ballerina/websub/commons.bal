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

import ballerina/crypto;
import ballerina/http;
import ballerina/io;
import ballerina/log;
import ballerina/mime;
import ballerina/reflect;

# Intent verification request parameter `hub.challenge` representing the challenge that needs to be echoed by
# susbscribers to verify intent.
@final string HUB_CHALLENGE = "hub.challenge";

# Parameter `hub.mode` representing the mode of the request from hub to subscriber or subscriber to hub.
@final string HUB_MODE = "hub.mode";

# Subscription change or intent verification request parameter `hub.topic` representing the topic relevant to the for
# which the request is initiated.
@final string HUB_TOPIC = "hub.topic";

# Subscription change request parameter `hub.callback` representing the callback to which notification should happen.
@final string HUB_CALLBACK = "hub.callback";

# Subscription request parameter `hub.lease_seconds` representing the period for which the subscription is expected to
# be active.
@final string HUB_LEASE_SECONDS = "hub.lease_seconds";

# Subscription parameter `hub.secret` representing the secret key to use for authenticated content distribution.
@final string HUB_SECRET = "hub.secret";

# `hub.mode` value indicating "subscription" mode, to subscribe to updates for a topic.
@final string MODE_SUBSCRIBE = "subscribe";

# `hub.mode` value indicating "unsubscription" mode, to unsubscribe to updates for a topic.
@final string MODE_UNSUBSCRIBE = "unsubscribe";

# `hub.mode` value indicating "publish" mode, used by a publisher to notify an update to a topic.
@final string MODE_PUBLISH = "publish";

# `hub.mode` value indicating "register" mode, used by a publisher to register a topic at a hub.
@final string MODE_REGISTER = "register";

# `hub.mode` value indicating "unregister" mode, used by a publisher to unregister a topic at a hub.
@final string MODE_UNREGISTER = "unregister";

# Topic registration parameter `publisher.secret` indicating a secret specified by a publisher when registering a
# topic at the hub, to use for authenticated content delivery between publisher and hub.
@final string PUBLISHER_SECRET = "publisher.secret";
@final string REMOTE_PUBLISHING_MODE_DIRECT = "direct";
@final string REMOTE_PUBLISHING_MODE_FETCH = "fetch";

@final string X_HUB_UUID = "X-Hub-Uuid";
@final string X_HUB_TOPIC = "X-Hub-Topic";
@final string X_HUB_SIGNATURE = "X-Hub-Signature";
@final string PUBLISHER_SIGNATURE = "Publisher-Signature";

@final string CONTENT_TYPE = "Content-Type";
@final string SHA1 = "SHA1";
@final string SHA256 = "SHA256";
@final string MD5 = "MD5";

@final string ANN_NAME_WEBSUB_SUBSCRIBER_SERVICE_CONFIG = "SubscriberServiceConfig";
@final string WEBSUB_PACKAGE_NAME = "ballerina/websub";


# The identifier to be used to identify the mode in which update content should be identified.
public type RemotePublishMode "PUBLISH_MODE_DIRECT"|"PUBLISH_MODE_FETCH";

# `RemotePublishMode` indicating direct update content notification (fat-ping). The payload of the update
# notification request from the publisher to the hub would include be the update content.
@final public RemotePublishMode PUBLISH_MODE_DIRECT = "PUBLISH_MODE_DIRECT";

# `RemotePublishMode` indicating that once the publisher notifies the hub that an update is available, the hub
# needs to fetch the topic URL to identify the update content.
@final public RemotePublishMode PUBLISH_MODE_FETCH = "PUBLISH_MODE_FETCH";

//TODO: Make public once extension story is finalized.
# The identifier to be used to identify the topic for dispatching with custom subscriber services.
public type TopicIdentifier "TOPIC_ID_HEADER"|"TOPIC_ID_PAYLOAD_KEY"|"TOPIC_ID_HEADER_AND_PAYLOAD";

# `TopicIdentifier` indicating dispatching based solely on a header of the request.
@final public TopicIdentifier TOPIC_ID_HEADER = "TOPIC_ID_HEADER";

# `TopicIdentifier` indicating dispatching based solely on a value for a key in the JSON payload of the request.
@final public TopicIdentifier TOPIC_ID_PAYLOAD_KEY = "TOPIC_ID_PAYLOAD_KEY";

# `TopicIdentifier` indicating dispatching based on a combination of header and values specified for a key/key(s) in
# the JSON payload of the request.
@final public TopicIdentifier TOPIC_ID_HEADER_AND_PAYLOAD = "TOPIC_ID_HEADER_AND_PAYLOAD";

///////////////////////////////////////////////////////////////////
//////////////////// WebSub Subscriber Commons ////////////////////
///////////////////////////////////////////////////////////////////
# Object representing an intent verification request received.
#
# + mode - The mode specified in the intent verification request, subscription or unsubscription
# + topic - The topic for which intent is verified to subscribe/unsubscribe
# + challenge - The challenge to be echoed to verify intent to subscribe/unsubscribe
# + leaseSeconds - The lease seconds period for which a subscription will be active if intent verification
#                  is being done for subscription
# + request - The HTTP request received for intent verification
public type IntentVerificationRequest object {

    public string mode;
    public string topic;
    public string challenge;
    public int leaseSeconds;
    public http:Request request;

    # Builds the response for the request, verifying intention to subscribe, if the topic matches that expected.
    #
    # + expectedTopic - The topic for which subscription should be accepted
    # + return - `http:Response` The response to the hub verifying/denying intent to subscribe
    public function buildSubscriptionVerificationResponse(string expectedTopic) returns http:Response;

    # Builds the response for the request, verifying intention to unsubscribe, if the topic matches that expected.
    #
    # + expectedTopic - The topic for which unsubscription should be accepted
    # + return - `http:Response` The response to the hub verifying/denying intent to unsubscribe
    public function buildUnsubscriptionVerificationResponse(string expectedTopic) returns http:Response;

};

function IntentVerificationRequest::buildSubscriptionVerificationResponse(string expectedTopic)
    returns http:Response {

    return buildIntentVerificationResponse(self, MODE_SUBSCRIBE, expectedTopic);
}

function IntentVerificationRequest::buildUnsubscriptionVerificationResponse(string expectedTopic)
    returns http:Response {

    return buildIntentVerificationResponse(self, MODE_UNSUBSCRIBE, expectedTopic);
}

# Function to build intent verification response for subscription/unsubscription requests sent.
#
# + intentVerificationRequest - The intent verification request from the hub
# + mode - The mode (subscription/unsubscription) for which a request was sent
# + topic - The intended topic for which subscription change should be verified
# + return - `http:Response` The response to the hub verifying/denying intent to subscripe/unsubscribe
function buildIntentVerificationResponse(IntentVerificationRequest intentVerificationRequest, string mode,
                                         string topic)
    returns http:Response {

    http:Response response = new;
    string reqTopic = check http:decode(intentVerificationRequest.topic, "UTF-8");
    if (topic == "") {
        response.statusCode = http:NOT_FOUND_404;
        log:printError("Intent Verification denied - Mode [" + mode + "], Topic [" + reqTopic +
                "], since topic unavailable as an annotation or unspecified as a parameter");
    } else {
        string reqMode = intentVerificationRequest.mode;
        string challenge = intentVerificationRequest.challenge;

        string reqLeaseSeconds = <string>intentVerificationRequest.leaseSeconds;

        if (reqMode == mode && reqTopic == topic) {
            response.statusCode = http:ACCEPTED_202;
            response.setTextPayload(challenge);
            log:printInfo("Intent Verification agreed - Mode [" + mode + "], Topic [" + topic + "], Lease Seconds ["
                    + reqLeaseSeconds + "]");
        } else {
            response.statusCode = http:NOT_FOUND_404;
            log:printWarn("Intent Verification denied - Mode [" + mode + "], Topic [" + reqTopic + "]");
        }
    }
    return response;
}

# Function to validate signature for requests received at the callback.
#
# + request - The request received
# + serviceType - The type of the service for which the request was rceived
# + return - `error`, if an error occurred in extraction or signature validation failed
function processWebSubNotification(http:Request request, typedesc serviceType) returns error? {
    string secret;
    match (retrieveSubscriberServiceAnnotations(serviceType)) {
        SubscriberServiceConfiguration subscriberServiceAnnotation => { secret = subscriberServiceAnnotation.secret; }
        () => { log:printDebug("WebSub notification received for subscription with no secret specified"); }
    }

    string xHubSignature;

    if (request.hasHeader(X_HUB_SIGNATURE)) {
        xHubSignature = request.getHeader(X_HUB_SIGNATURE);
    } else {
        if (secret != "") {
            error webSubError = {message:X_HUB_SIGNATURE + " header not present for subscription added" +
                " specifying " + HUB_SECRET};
            return webSubError;
        }
        return;
    }

    if (secret == "" && xHubSignature != "") {
        log:printWarn("Ignoring " + X_HUB_SIGNATURE + " value since secret is not specified.");
        return;
    }

    string stringPayload;
    match (request.getPayloadAsString()) {
        string payloadAsString => { stringPayload = payloadAsString; }
        error entityError => {
            error webSubError = {message:"Error extracting notification payload as string for signature validation: "
                                            + entityError.message, cause: entityError};
            return webSubError;
        }
    }

    return validateSignature(xHubSignature, stringPayload, secret);
}

# Function to validate the signature header included in the notification.
#
# + xHubSignature - The X-Hub-Signature header included in the notification request from the hub
# + stringPayload - The string representation of the notification payload received
# + secret - The secret used when subscribing
# + return - `error` if an error occurs validating the signature or the signature is invalid
function validateSignature(string xHubSignature, string stringPayload, string secret) returns error? {

    string[] splitSignature = xHubSignature.split("=");
    string method = splitSignature[0];
    string signature = xHubSignature.replace(method + "=", "");
    string generatedSignature;

    if (SHA1.equalsIgnoreCase(method)) {
        generatedSignature = crypto:hmac(stringPayload, secret, crypto:SHA1);
    } else if (SHA256.equalsIgnoreCase(method)) {
        generatedSignature = crypto:hmac(stringPayload, secret, crypto:SHA256);
    } else if (MD5.equalsIgnoreCase(method)) {
        generatedSignature = crypto:hmac(stringPayload, secret, crypto:MD5);
    } else {
        error webSubError = {message:"Unsupported signature method: " + method};
        return webSubError;
    }

    if (!signature.equalsIgnoreCase(generatedSignature)) {
        error webSubError = {message:"Signature validation failed: Invalid Signature!"};
        return webSubError;
    }
    return;
}

# Object representing the WebSub Content Delivery Request received.
#
# + request - The HTTP POST request received as the notification
public type Notification object {

    private http:Request request;

    # Retrieves the query parameters of the content delivery request, as a map.
    #
    # + return - String constrained map of query params
    public function getQueryParams() returns map<string> {
        return request.getQueryParams();
    }

    # Retrieves the `Entity` associated with the content delivery request.
    #
    # + return - The `Entity` of the request. An `error` is returned, if entity construction fails
    public function getEntity() returns mime:Entity|error {
        return request.getEntity();
    }

    # Returns whether the requested header key exists in the header map of the content delivery request.
    #
    # + headerName - The header name
    # + return - Returns true if the specified header key exists
    public function hasHeader(string headerName) returns boolean {
        return request.hasHeader(headerName);
    }

    # Returns the value of the specified header. If the specified header key maps to multiple values, the first of
    # these values is returned.
    #
    # + headerName - The header name
    # + return - The first header value for the specified header name. An exception is thrown if no header is found.
    #            Ideally `hasHeader()` needs to be used to check the existence of header initially.
    public function getHeader(string headerName) returns string {
        return request.getHeader(headerName);
    }

    # Retrieves all the header values to which the specified header key maps to.
    #
    # + headerName - The header name
    # + return - The header values the specified header key maps to. An exception is thrown if no header is found.
    #            Ideally `hasHeader()` needs to be used to check the existence of header initially.
    public function getHeaders(string headerName) returns string[] {
        return request.getHeaders(headerName);
    }

    # Retrieves all the names of the headers present in the content delivery request.
    #
    # + return - An array of all the header names
    public function getHeaderNames() returns string[] {
        return request.getHeaderNames();
    }

    # Retrieves the type of the payload of the content delivery request (i.e: the `content-type` header value).
    #
    # + return - Returns the `content-type` header value as a string
    public function getContentType() returns string {
        return request.getContentType();
    }

    # Extracts `json` payload from the content delivery request.
    #
    # + return - The `json` payload or `error` in case of errors. If the content type is not JSON, an `error` is returned.
    public function getJsonPayload() returns json|error {
        return request.getJsonPayload();
    }

    # Extracts `xml` payload from the content delivery request.
    #
    # + return - The `xml` payload or `error` in case of errors. If the content type is not XML, an `error` is returned.
    public function getXmlPayload() returns xml|error {
        return request.getXmlPayload();
    }

    # Extracts `text` payload from the content delivery request.
    #
    # + return - The `text` payload or `error` in case of errors.
    #            If the content type is not of type text, an `error` is returned.
    public function getTextPayload() returns string|error {
        return request.getTextPayload();
    }

    # Retrieves the content delivery request payload as a `string`. Content type is not checked during payload
    # construction which makes this different from `getTextPayload()` function.
    #
    # + return - The string representation of the message payload or `error` in case of errors
    public function getPayloadAsString() returns string|error {
        return request.getPayloadAsString();
    }

    # Retrieves the request payload as a `ByteChannel` except in the case of multiparts.
    #
    # + return - A byte channel from which the message payload can be read or `error` in case of errors
    public function getByteChannel() returns io:ByteChannel|error {
        return request.getByteChannel();
    }

    # Retrieves the request payload as a `byte[]`.
    #
    # + return - The byte[] representation of the message payload or `error` in case of errors
    public function getBinaryPayload() returns byte[]|error {
        return request.getBinaryPayload();
    }

    # Retrieves the form parameters from the content delivery request as a `map`.
    #
    # + return - The map of form params or `error` in case of errors
    public function getFormParams() returns map<string>|error {
        return request.getFormParams();
    }

};

# Record representing a WebSub subscription change request.
#
# + topic - The topic for which the subscription/unsubscription request is sent
# + callback - The callback which should be registered/unregistered for the subscription/unsubscription request sent
# + leaseSeconds - The lease period for which the subscription is expected to be active
# + secret - The secret to be used for authenticated content distribution with this subscription
public type SubscriptionChangeRequest record {
    string topic;
    string callback;
    int leaseSeconds;
    string secret;
    !...
};

# Record representing subscription/unsubscription details if a subscription/unsubscription request is successful.
#
# + hub - The hub at which the subscription/unsubscription was successful
# + topic - The topic for which the subscription/unsubscription was successful
# + response - The response from the hub to the subscription/unsubscription request
public type SubscriptionChangeResponse record {
    string hub;
    string topic;
    http:Response response;
    !...
};

/////////////////////////////////////////////////////////////
//////////////////// WebSub Hub Commons /////////////////////
/////////////////////////////////////////////////////////////
# Starts up the Ballerina Hub.
#
# + port - The port to start up the hub on
# + leaseSeconds - The default lease seconds value to honour if not specified in subscription requests
# + signatureMethod - The signature method to use for authenticated content delivery (`SHA1`|`SHA256`)
# + remotePublishingEnabled - Whether remote publishers should be allowed to publish to this hub (HTTP requests)
# + remotePublishMode - If remote publishing is allowed, the mode to use, `direct` (default) - fat ping with
#                          the notification payload specified or `fetch` - the hub fetches the topic URL
#                          specified in the "publish" request to identify the payload
# + topicRegistrationRequired - Whether a topic needs to be registered at the hub prior to publishing/subscribing
#                               to the topic
# + publicUrl - The URL for the hub to be included in content delivery requests, defaults to
#               `http(s)://localhost:{port}/websub/hub` if unspecified
# + sslEnabled - Whether SSL needs to be enabled for the hub, enabled by default
# + serviceSecureSocket - The SSL configuration for the hub service endpoint
# + clientSecureSocket - The SSL configuration for the hub service for secure communication with remote HTTP
#                        endpoints
# + return - `WebSubHub` The WebSubHub object representing the newly started up hub, or `HubStartedUpError` indicating
#            that the hub is already started, and including the WebSubHub object representing the
#            already started up hub
public function startHub(int port, int? leaseSeconds = (), string? signatureMethod = (),
boolean? remotePublishingEnabled = (), RemotePublishMode? remotePublishMode = (),
boolean? topicRegistrationRequired = (), string? publicUrl = (),
boolean? sslEnabled = (), http:ServiceSecureSocket? serviceSecureSocket = (),
http:SecureSocket? clientSecureSocket = ())
    returns WebSubHub|HubStartedUpError {

    hubPort = config:getAsInt("b7a.websub.hub.port", default = port);
    hubLeaseSeconds = config:getAsInt("b7a.websub.hub.leasetime",
                                      default = leaseSeconds but { () => DEFAULT_LEASE_SECONDS_VALUE });
    hubSignatureMethod = config:getAsString("b7a.websub.hub.signaturemethod",
                                            default = signatureMethod but { () => DEFAULT_SIGNATURE_METHOD });
    hubRemotePublishingEnabled = config:getAsBoolean("b7a.websub.hub.remotepublish",
                                                     default = remotePublishingEnabled but { () => false });

    string remotePublishModeAsConfig =  config:getAsString("b7a.websub.hub.remotepublish.mode");
    if (remotePublishModeAsConfig == "") {
        hubRemotePublishMode = remotePublishMode but { () => PUBLISH_MODE_DIRECT };
    } else {
        if (REMOTE_PUBLISHING_MODE_FETCH.equalsIgnoreCase(remotePublishModeAsConfig)) {
            hubRemotePublishMode = PUBLISH_MODE_FETCH;
        } else if (!REMOTE_PUBLISHING_MODE_DIRECT.equalsIgnoreCase(remotePublishModeAsConfig)) {
            log:printWarn("unknown publish mode: [" + remotePublishModeAsConfig + "], defaulting to direct mode");
        }
    }

    hubTopicRegistrationRequired = config:getAsBoolean("b7a.websub.hub.topicregistration",
                                                       default = topicRegistrationRequired but { () => true });
    hubSslEnabled = config:getAsBoolean("b7a.websub.hub.enablessl", default = sslEnabled but { () => true });
    //set serviceSecureSocket after hubSslEnabled is set
    if (hubSslEnabled) {
        hubServiceSecureSocket = getServiceSecureSocketConfig(serviceSecureSocket);
    }
    hubClientSecureSocket = getSecureSocketConfig(clientSecureSocket);
    //reset the hubUrl once the other parameters are set
    hubPublicUrl = config:getAsString("b7a.websub.hub.url", default = publicUrl but { () => getHubUrl() });
    return startUpHubService(hubTopicRegistrationRequired, hubPublicUrl);
}

# Object representing a Ballerina WebSub Hub.
#
# + hubUrl - The URL of the started up Ballerina WebSub Hub
# + hubServiceEndpoint - The HTTP endpoint to which the Ballerina WebSub Hub is bound
public type WebSubHub object {

    public string hubUrl;
    private http:Listener hubServiceEndpoint;

    new (hubUrl, hubServiceEndpoint) {}

    # Stops the started up Ballerina WebSub Hub.
    #
    # + return - `boolean` indicating whether the internal Ballerina Hub was stopped
    public function stop() returns boolean;

    # Publishes an update against the topic in the initialized Ballerina Hub.
    #
    # + topic - The topic for which the update should happen
    # + payload - The update payload
    # + contentType - The content type header to set for the request delivering the payload
    # + return - `error` if the hub is not initialized or does not represent the internal hub
    public function publishUpdate(string topic, string|xml|json|byte[]|io:ByteChannel payload,
                                  string? contentType = ()) returns error?;

    # Registers a topic in the Ballerina Hub.
    #
    # + topic - The topic to register
    # + return - `error` if an error occurred with registration
    public function registerTopic(string topic) returns error?;

    # Unregisters a topic in the Ballerina Hub.
    #
    # + topic - The topic to unregister
    # + return - `error` if an error occurred with unregistration
    public function unregisterTopic(string topic) returns error?;

};

function WebSubHub::stop() returns boolean {
    self.hubServiceEndpoint.stop();
    return stopHubService(self.hubUrl);
}

function WebSubHub::publishUpdate(string topic, string|xml|json|byte[]|io:ByteChannel payload,
                                         string? contentType = ()) returns error? {

    if (self.hubUrl == "") {
        error webSubError = {message: "Internal Ballerina Hub not initialized or incorrectly referenced"};
        return webSubError;
    }

    WebSubContent content = {};

    match(payload) {
        io:ByteChannel byteChannel => content.payload = constructByteArray(byteChannel);
        string|xml|json|byte[] => content.payload = payload;
    }

    match(contentType) {
        string stringContentType => content.contentType = stringContentType;
        () => {
            match(payload) {
                string => content.contentType = mime:TEXT_PLAIN;
                xml => content.contentType = mime:APPLICATION_XML;
                json => content.contentType = mime:APPLICATION_JSON;
                byte[]|io:ByteChannel => content.contentType = mime:APPLICATION_OCTET_STREAM;
            }
        }
    }

    return validateAndPublishToInternalHub(self.hubUrl, topic, content);
}

function WebSubHub::registerTopic(string topic) returns error? {
    return registerTopicAtHub(topic, "");
}

function WebSubHub::unregisterTopic(string topic) returns error? {
    return unregisterTopicAtHub(topic, "");
}

///////////////////////////////////////////////////////////////////
//////////////////// WebSub Publisher Commons /////////////////////
///////////////////////////////////////////////////////////////////
# Function to add link headers to a response to allow WebSub discovery.
#
# + response - The response being sent
# + hubs - The hubs the publisher advertises as the hubs that it publishes updates to
# + topic - The topic to which subscribers need to subscribe to, to receive updates for the resource
public function addWebSubLinkHeader(http:Response response, string[] hubs, string topic) {
    string hubLinkHeader = "";
    foreach hub in hubs {
        hubLinkHeader = hubLinkHeader + "<" + hub + ">; rel=\"hub\", ";
    }
    response.setHeader("Link", hubLinkHeader + "<" + topic + ">; rel=\"self\"");
}

# Struct to represent Subscription Details retrieved from the database.
#
# + topic - The topic for which the subscription is added
# + callback - The callback specified for the particular subscription
# + secret - The secret to be used for authenticated content distribution
# + leaseSeconds - The lease second period specified for the particular subscription
# + createdAt - The time at which the subscription was created
type SubscriptionDetails record {
    string topic;
    string callback;
    string secret;
    int leaseSeconds;
    int createdAt;
    !...
};

function retrieveSubscriberServiceAnnotations(typedesc serviceType) returns SubscriberServiceConfiguration? {
    reflect:annotationData[] annotationDataArray = reflect:getServiceAnnotations(serviceType);
    foreach annData in annotationDataArray {
        if (annData.name == ANN_NAME_WEBSUB_SUBSCRIBER_SERVICE_CONFIG && annData.pkgName == WEBSUB_PACKAGE_NAME) {
            SubscriberServiceConfiguration subscriberServiceAnnotation =
                                                            check <SubscriberServiceConfiguration> (annData.value);
            return subscriberServiceAnnotation;
        }
    }
    return;
}

# Record to represent a WebSub content delivery.
#
# + payload - The payload to be sent
# + contentType - The content-type of the payload
type WebSubContent record {
    string|xml|json|byte[]|io:ByteChannel payload;
    string contentType;
    !...
};

function isSuccessStatusCode(int statusCode) returns boolean {
    return (200 <= statusCode && statusCode < 300);
}

# Error to represent that a WebSubHub is already started up, encapsulating the started up Hub.
#
# + message - The error message
# + cause - The cause of the `HubStartedUpError`, if available
# + startedUpHub - The `WebSubHub` object representing the started up Hub
public type HubStartedUpError record {
    string message;
    error? cause;
    WebSubHub startedUpHub;
    !...
};
