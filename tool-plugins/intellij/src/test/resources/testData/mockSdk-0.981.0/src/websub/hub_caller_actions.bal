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
import ballerina/log;
import ballerina/mime;
import ballerina/crypto;

documentation {
    The HTTP based Caller actions for outbound WebSub Subscription, Unsubscription, Registration, Unregistration and
    Notification requests to a Hub.

    F{{hubUrl}} The URL of the target Hub to which requests need to be sent
}
public type CallerActions object {

    public string hubUrl;

    private http:Client httpClientEndpoint;
    private http:FollowRedirects? followRedirects;

    new (hubUrl, httpClientEndpoint, followRedirects) {}

    documentation {
        Sends a subscription request to a WebSub Hub.

        P{{subscriptionRequest}} The `SubscriptionChangeRequest` containing subscription details
        R{{}} `SubscriptionChangeResponse` indicating subscription details, if the request was successful else
                `error` if an error occurred with the subscription request
    }
    public function subscribe(SubscriptionChangeRequest subscriptionRequest)
        returns @tainted (SubscriptionChangeResponse|error);

    documentation {
        Sends an unsubscription request to a WebSub Hub.

        P{{unsubscriptionRequest}} The `SubscriptionChangeRequest` containing unsubscription details
        R{{}} `SubscriptionChangeResponse` indicating unsubscription details, if the request was successful else
                `error` if an error occurred with the unsubscription request
    }
    public function unsubscribe(SubscriptionChangeRequest unsubscriptionRequest)
        returns @tainted (SubscriptionChangeResponse|error);

    documentation {
        Registers a topic in a Ballerina WebSub Hub against which subscribers can subscribe and the publisher will
         publish updates, with a secret which will be used in signature generation if specified.

        P{{topic}} The topic to register
        P{{secret}} The secret the publisher will use to generate a signature when publishing updates
        R{{}} `error` if an error occurred registering the topic
    }
    public function registerTopic(string topic, string? secret = ()) returns error?;

    documentation {
        Unregisters a topic in a Ballerina WebSub Hub.

        P{{topic}} The topic to unregister
        P{{secret}} The secret the publisher used when registering the topic
        R{{}} `error` if an error occurred unregistering the topic
    }
    public function unregisterTopic(string topic, string? secret = ()) returns error?;

    documentation {
        Publishes an update to a remote Ballerina WebSub Hub.

        P{{topic}} The topic for which the update occurred
        P{{payload}} The update payload
        P{{secret}} The secret used when registering the topic
        P{{signatureMethod}} The signature method to use to generate a secret
        P{{headers}} The headers, if any, that need to be set
        R{{}} `error` if an error occurred with the update
    }
    public function publishUpdate(string topic, string|xml|json|byte[]|io:ByteChannel payload, string? contentType = (),
                                  string? secret = (), string signatureMethod = "sha256", map<string>? headers = ())
        returns error?;

    documentation {
        Notifies a remote WebSub Hub that an update is available to fetch, for hubs that require publishing to
         happen as such.

        P{{topic}} The topic for which the update occurred
        P{{headers}} The headers, if any, that need to be set
        R{{}} `error` if an error occurred with the notification
    }
    public function notifyUpdate(string topic, map<string>? headers = ()) returns error?;
};

function CallerActions::subscribe(SubscriptionChangeRequest subscriptionRequest)
    returns @tainted SubscriptionChangeResponse|error {

    endpoint http:Client httpClientEndpoint = self.httpClientEndpoint;
    http:Request builtSubscriptionRequest = buildSubscriptionChangeRequest(MODE_SUBSCRIBE, subscriptionRequest);
    var response = httpClientEndpoint->post("", builtSubscriptionRequest);
    int redirectCount = getRedirectionMaxCount(self.followRedirects);
    return processHubResponse(self.hubUrl, MODE_SUBSCRIBE, subscriptionRequest, response, httpClientEndpoint,
                              redirectCount);
}

function CallerActions::unsubscribe(SubscriptionChangeRequest unsubscriptionRequest)
    returns @tainted SubscriptionChangeResponse|error {

    endpoint http:Client httpClientEndpoint = self.httpClientEndpoint;
    http:Request builtUnsubscriptionRequest = buildSubscriptionChangeRequest(MODE_UNSUBSCRIBE, unsubscriptionRequest);
    var response = httpClientEndpoint->post("", builtUnsubscriptionRequest);
    int redirectCount = getRedirectionMaxCount(self.followRedirects);
    return processHubResponse(self.hubUrl, MODE_UNSUBSCRIBE, unsubscriptionRequest, response, httpClientEndpoint,
                              redirectCount);
}

function CallerActions::registerTopic(string topic, string? secret = ()) returns error? {
    endpoint http:Client httpClientEndpoint = self.httpClientEndpoint;
    http:Request request = buildTopicRegistrationChangeRequest(MODE_REGISTER, topic, secret = secret);
    var registrationResponse = httpClientEndpoint->post("", request);
    match (registrationResponse) {
        http:Response response => {
            if (response.statusCode != http:ACCEPTED_202) {
                string payload = response.getTextPayload() but { error => "" };
                error webSubError = {message:"Error occured during topic registration: " + payload};
                return webSubError;
            }
            return;
        }
        error err => {
            error webSubError = {message:"Error sending topic registration request: " + err.message,
                cause:err};
            return webSubError;
        }
    }
}

function CallerActions::unregisterTopic(string topic, string? secret = ()) returns error? {
    endpoint http:Client httpClientEndpoint = self.httpClientEndpoint;
    http:Request request = buildTopicRegistrationChangeRequest(MODE_UNREGISTER, topic, secret = secret);
    var unregistrationResponse = httpClientEndpoint->post("", request);
    match (unregistrationResponse) {
        http:Response response => {
            if (response.statusCode != http:ACCEPTED_202) {
                string payload = response.getTextPayload() but { error => "" };
                error webSubError = {message:"Error occured during topic unregistration: " + payload};
                return webSubError;
            }
            return;
        }
        error err => {
            error webSubError = {message:"Error sending topic unregistration request: " + err.message,
                cause:err};
            return webSubError;
        }
    }
}

function CallerActions::publishUpdate(string topic, string|xml|json|byte[]|io:ByteChannel payload,
                                             string? contentType = (), string? secret = (),
                                             string signatureMethod = "sha256", map<string>? headers = ())
        returns error? {

    endpoint http:Client httpClientEndpoint = self.httpClientEndpoint;
    http:Request request = new;
    string queryParams = HUB_MODE + "=" + MODE_PUBLISH + "&" + HUB_TOPIC + "=" + topic;
    request.setPayload(payload);

    match(contentType) {
        string specifiedContentType => request.setContentType(specifiedContentType);
        () => {}
    }

    match (secret) {
        string specifiedSecret => {
            string stringPayload = request.getPayloadAsString() but { error => "" };
            string publisherSignature = signatureMethod + "=";
            string generatedSignature = "";
            if (SHA1.equalsIgnoreCase(signatureMethod)) {
                generatedSignature = crypto:hmac(stringPayload, specifiedSecret, crypto:SHA1);
            } else if (SHA256.equalsIgnoreCase(signatureMethod)) {
                generatedSignature = crypto:hmac(stringPayload, specifiedSecret, crypto:SHA256);
            } else if (MD5.equalsIgnoreCase(signatureMethod)) {
                generatedSignature = crypto:hmac(stringPayload, specifiedSecret, crypto:MD5);
            }
            publisherSignature = publisherSignature + generatedSignature;
            request.setHeader(PUBLISHER_SIGNATURE, publisherSignature);
        }
        () => {}
    }

    match (headers) {
        map<string> headerMap => {
            foreach key, value in headerMap {
                request.setHeader(key, value);
            }
        }
        () => {}
    }

    var response = httpClientEndpoint->post(untaint ("?" + queryParams), request);
    match (response) {
        http:Response httpResponse => {
            if (!isSuccessStatusCode(httpResponse.statusCode)) {
                string textPayload = httpResponse.getTextPayload() but { error => "" };
                error webSubError = {message:"Error occured publishing update: " + textPayload };
                return webSubError;
            }
            return;
        }
        error httpConnectorError => {
            error webSubError = {message: "Publish failed for topic [" + topic + "]", cause:httpConnectorError};
            return webSubError;
        }
    }
}

function CallerActions::notifyUpdate(string topic, map<string>? headers = ()) returns error? {
    endpoint http:Client httpClientEndpoint = self.httpClientEndpoint;
    http:Request request = new;
    string queryParams = HUB_MODE + "=" + MODE_PUBLISH + "&" + HUB_TOPIC + "=" + topic;

    match (headers) {
        map<string> headerMap => {
            foreach key, value in headerMap {
                request.setHeader(key, value);
            }
        }
        () => {}
    }

    var response = httpClientEndpoint->post(untaint ("?" + queryParams), request);
    match (response) {
        http:Response httpResponse => {
            if (!isSuccessStatusCode(httpResponse.statusCode)) {
                string textPayload = httpResponse.getTextPayload() but { error => "" };
                error webSubError = {message:"Error occured notifying update availability: " + textPayload };
                return webSubError;
            }
            return;
        }
        error httpConnectorError => {
            error webSubError = {message:"Update availability notification failed for topic [" + topic + "]",
                                 cause:httpConnectorError};
            return webSubError;
        }
    }
}

documentation {
    Builds the topic registration change request to register or unregister a topic at the hub.

    P{{mode}} Whether the request is for registration or unregistration
    P{{topic}} The topic to register/unregister
    P{{secret}} The secret associated with this topic that will be used to validate updates
    R{{}} `http:Request` The Request to send to the hub to register/unregister
}
function buildTopicRegistrationChangeRequest(@sensitive string mode, @sensitive string topic,
                                             @sensitive string? secret = ()) returns (http:Request) {
    http:Request request = new;
    string body = HUB_MODE + "=" + mode + "&" + HUB_TOPIC + "=" + topic;
    match (secret) {
        string specifiedSecret => { body = body + "&" + PUBLISHER_SECRET + "=" + specifiedSecret; }
        () => {}
    }
    request.setTextPayload(body);
    request.setHeader(CONTENT_TYPE, mime:APPLICATION_FORM_URLENCODED);
    return request;
}

documentation {
    Function to build the subscription request to subscribe at the hub.

    P{{mode}} Whether the request is for subscription or unsubscription
    P{{subscriptionChangeRequest}} The SubscriptionChangeRequest specifying the topic to subscribe to and the
                                    parameters to use
    R{{}} `http:Request` The Request to send to the hub to subscribe/unsubscribe
}
function buildSubscriptionChangeRequest(@sensitive string mode,
                                        SubscriptionChangeRequest subscriptionChangeRequest) returns (http:Request) {
    http:Request request = new;
    string body = HUB_MODE + "=" + mode
        + "&" + HUB_TOPIC + "=" + subscriptionChangeRequest.topic
        + "&" + HUB_CALLBACK + "=" + subscriptionChangeRequest.callback;
    if (mode == MODE_SUBSCRIBE) {
        if (subscriptionChangeRequest.secret.trim() != "") {
            body = body + "&" + HUB_SECRET + "=" + subscriptionChangeRequest.secret;
        }
        if (subscriptionChangeRequest.leaseSeconds != 0) {
            body = body + "&" + HUB_LEASE_SECONDS + "=" + subscriptionChangeRequest.leaseSeconds;
        }
    }
    request.setTextPayload(body);
    request.setHeader(CONTENT_TYPE, mime:APPLICATION_FORM_URLENCODED);
    return request;
}

documentation {
    Function to process the response from the hub on subscription/unsubscription and extract required information.

    P{{hub}} The hub to which the subscription/unsubscription request was sent
    P{{mode}} Whether the request was sent for subscription or unsubscription
    P{{subscriptionChangeRequest}} The subscription change request sent
    P{{response}} The http:Response or error received on request to the hub
    P{{httpClientEndpoint}} The underlying HTTP Client Endpoint
    R{{}} `SubscriptionChangeResponse` indicating subscription/unsubscription details, if the request was successful
            else `error` if an error occurred
}
function processHubResponse(@sensitive string hub, @sensitive string mode,
                            SubscriptionChangeRequest subscriptionChangeRequest,
                            http:Response|error response, http:Client httpClientEndpoint,
                            int remainingRedirects)
    returns @tainted SubscriptionChangeResponse|error {

    string topic = subscriptionChangeRequest.topic;
    match response {
        error httpConnectorError => {
            string errorMessage = "Error occurred for request: Mode[" + mode + "] at Hub[" + hub + "] - "
                + httpConnectorError.message;
            error webSubError = {message:errorMessage, cause:httpConnectorError};
            return webSubError;
        }
        http:Response httpResponse => {
            int responseStatusCode = httpResponse.statusCode;
            if (responseStatusCode == http:TEMPORARY_REDIRECT_307
                    || responseStatusCode == http:PERMANENT_REDIRECT_308) {
                if (remainingRedirects > 0) {
                    string redirected_hub = httpResponse.getHeader("Location");
                    return invokeClientConnectorOnRedirection(redirected_hub, mode, subscriptionChangeRequest,
                                                                httpClientEndpoint.config.auth, remainingRedirects - 1);
                }
                error subscriptionError = { message: "Redirection response received for subscription change request"
                                            + " made with followRedirects disabled or after maxCount exceeded: Hub ["
                                            + hub + "], Topic [" + subscriptionChangeRequest.topic + "]" };
                return subscriptionError;
            } else if (!isSuccessStatusCode(responseStatusCode)) {
                var responsePayload = httpResponse.getTextPayload();
                string errorMessage = "Error in request: Mode[" + mode + "] at Hub[" + hub + "]";
                match (responsePayload) {
                    string responseErrorPayload => { errorMessage = errorMessage + " - " + responseErrorPayload; }
                    error payloadError => { errorMessage = errorMessage + " - "
                        + "Error occurred identifying cause: "
                        + payloadError.message; }
                }
                error webSubError = {message:errorMessage};
                return webSubError;
            } else {
                if (responseStatusCode != http:ACCEPTED_202) {
                    log:printDebug("Subscription request considered successful for non 202 status code: "
                                    + responseStatusCode);
                }
                SubscriptionChangeResponse subscriptionChangeResponse = {hub:hub, topic:topic, response:httpResponse};
                return subscriptionChangeResponse;
            }
        }
    }
}

documentation {
    Function to invoke the WebSubSubscriberConnector's actions for subscription/unsubscription on redirection from the
    original hub.

    P{{hub}} The hub to which the subscription/unsubscription request is to be sent
    P{{mode}} Whether the request is for subscription or unsubscription
    P{{subscriptionChangeRequest}} The request containing subscription/unsubscription details
    P{{auth}} The auth config to use at the hub, if specified
    R{{}} `SubscriptionChangeResponse` indicating subscription/unsubscription details, if the request was successful
            else `error` if an error occurred
}
function invokeClientConnectorOnRedirection(@sensitive string hub, @sensitive string mode, SubscriptionChangeRequest
                                            subscriptionChangeRequest, http:AuthConfig? auth, int remainingRedirects)
    returns @tainted SubscriptionChangeResponse|error {

    if (mode == MODE_SUBSCRIBE) {
        return subscribeWithRetries(hub, subscriptionChangeRequest, auth, remainingRedirects = remainingRedirects);
    } else {
        return unsubscribeWithRetries(hub, subscriptionChangeRequest, auth, remainingRedirects = remainingRedirects);
    }
}

function subscribeWithRetries(string hubUrl, SubscriptionChangeRequest subscriptionRequest, http:AuthConfig? auth,
                              int remainingRedirects = 0) returns @tainted SubscriptionChangeResponse| error {
    endpoint http:Client clientEndpoint {
        url:hubUrl,
        auth:auth
    };
    http:Request builtSubscriptionRequest = buildSubscriptionChangeRequest(MODE_SUBSCRIBE, subscriptionRequest);
    var response = clientEndpoint->post("", builtSubscriptionRequest);
    return processHubResponse(hubUrl, MODE_SUBSCRIBE, subscriptionRequest, response, clientEndpoint,
                              remainingRedirects);
}

function unsubscribeWithRetries(string hubUrl, SubscriptionChangeRequest unsubscriptionRequest, http:AuthConfig? auth,
                                int remainingRedirects = 0) returns @tainted SubscriptionChangeResponse|error {
    endpoint http:Client clientEndpoint {
        url:hubUrl,
        auth:auth
    };
    http:Request builtSubscriptionRequest = buildSubscriptionChangeRequest(MODE_UNSUBSCRIBE, unsubscriptionRequest);
    var response = clientEndpoint->post("", builtSubscriptionRequest);
    return processHubResponse(hubUrl, MODE_UNSUBSCRIBE, unsubscriptionRequest, response, clientEndpoint,
                              remainingRedirects);
}

function getRedirectionMaxCount(http:FollowRedirects? followRedirects) returns int {
    match(followRedirects) {
        http:FollowRedirects newFollowRedirects => {
            if (newFollowRedirects.enabled) {
                return newFollowRedirects.maxCount;
            }
        }
        () => {}
    }
    return 0;
}
