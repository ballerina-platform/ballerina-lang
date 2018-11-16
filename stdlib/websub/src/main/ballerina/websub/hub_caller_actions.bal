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

# The HTTP based Caller actions for outbound WebSub Subscription, Unsubscription, Registration, Unregistration and
# Notification requests to a Hub.
#
# + hubUrl - The URL of the target Hub to which requests need to be sent
public type CallerActions object {

    public string hubUrl;

    private http:Client httpClientEndpoint;
    private http:FollowRedirects? followRedirects;

    new (hubUrl, httpClientEndpoint, followRedirects) {}

    # Sends a subscription request to a WebSub Hub.
    #
    # + subscriptionRequest - The `SubscriptionChangeRequest` containing subscription details
    # + return - `SubscriptionChangeResponse` indicating subscription details, if the request was successful else
    #            `error` if an error occurred with the subscription request
    public function subscribe(SubscriptionChangeRequest subscriptionRequest)
        returns @tainted SubscriptionChangeResponse|error;

    # Sends an unsubscription request to a WebSub Hub.
    #
    # + unsubscriptionRequest - The `SubscriptionChangeRequest` containing unsubscription details
    # + return - `SubscriptionChangeResponse` indicating unsubscription details, if the request was successful else
    #            `error` if an error occurred with the unsubscription request
    public function unsubscribe(SubscriptionChangeRequest unsubscriptionRequest)
        returns @tainted SubscriptionChangeResponse|error;

    # Registers a topic in a Ballerina WebSub Hub against which subscribers can subscribe and the publisher will
    # publish updates.
    #
    # + topic - The topic to register
    # + return - `error` if an error occurred registering the topic
    public function registerTopic(string topic) returns error?;

    # Unregisters a topic in a Ballerina WebSub Hub.
    #
    # + topic - The topic to unregister
    # + return - `error` if an error occurred unregistering the topic
    public function unregisterTopic(string topic) returns error?;

    # Publishes an update to a remote Ballerina WebSub Hub.
    #
    # + topic - The topic for which the update occurred
    # + payload - The update payload
    # + contentType - The type of the update content, to set as the `ContentType` header
    # + headers - The headers, if any, that need to be set
    # + return - `error` if an error occurred with the update
    public function publishUpdate(string topic, string|xml|json|byte[]|io:ReadableByteChannel payload, string? contentType = (),
                                  map<string>? headers = ()) returns error?;

    # Notifies a remote WebSub Hub that an update is available to fetch, for hubs that require publishing to
    # happen as such.
    #
    # + topic - The topic for which the update occurred
    # + headers - The headers, if any, that need to be set
    # + return - `error` if an error occurred with the notification
    public function notifyUpdate(string topic, map<string>? headers = ()) returns error?;
};

function CallerActions.subscribe(SubscriptionChangeRequest subscriptionRequest)
    returns @tainted SubscriptionChangeResponse|error {

    endpoint http:Client httpClientEndpoint = self.httpClientEndpoint;
    http:Request builtSubscriptionRequest = buildSubscriptionChangeRequest(MODE_SUBSCRIBE, subscriptionRequest);
    var response = httpClientEndpoint->post("", builtSubscriptionRequest);
    int redirectCount = getRedirectionMaxCount(self.followRedirects);
    return processHubResponse(self.hubUrl, MODE_SUBSCRIBE, subscriptionRequest, response, httpClientEndpoint,
                              redirectCount);
}

function CallerActions.unsubscribe(SubscriptionChangeRequest unsubscriptionRequest)
    returns @tainted SubscriptionChangeResponse|error {

    endpoint http:Client httpClientEndpoint = self.httpClientEndpoint;
    http:Request builtUnsubscriptionRequest = buildSubscriptionChangeRequest(MODE_UNSUBSCRIBE, unsubscriptionRequest);
    var response = httpClientEndpoint->post("", builtUnsubscriptionRequest);
    int redirectCount = getRedirectionMaxCount(self.followRedirects);
    return processHubResponse(self.hubUrl, MODE_UNSUBSCRIBE, unsubscriptionRequest, response, httpClientEndpoint,
                              redirectCount);
}

function CallerActions.registerTopic(string topic) returns error? {
    endpoint http:Client httpClientEndpoint = self.httpClientEndpoint;
    http:Request request = buildTopicRegistrationChangeRequest(MODE_REGISTER, topic);
    var registrationResponse = httpClientEndpoint->post("", request);
    if (registrationResponse is http:Response) {
        if (registrationResponse.statusCode != http:ACCEPTED_202) {
            var result = registrationResponse.getTextPayload();
            string payload = result is string ? result : "";
            map errorDetail = { message : "Error occured during topic registration: " + payload };
            error webSubError = error(WEBSUB_ERROR_CODE, errorDetail);
            return webSubError;
        }
    } else if (registrationResponse is error) {
        string errCause = <string> registrationResponse.detail().message;
        map errorDetail = { message : "Error sending topic registration request: " + errCause };
        error webSubError = error(WEBSUB_ERROR_CODE, errorDetail);
        return webSubError;
    }
    return;
}

function CallerActions.unregisterTopic(string topic) returns error? {
    endpoint http:Client httpClientEndpoint = self.httpClientEndpoint;
    http:Request request = buildTopicRegistrationChangeRequest(MODE_UNREGISTER, topic);
    var unregistrationResponse = httpClientEndpoint->post("", request);
    if (unregistrationResponse is http:Response) {
        if (unregistrationResponse.statusCode != http:ACCEPTED_202) {
            var result = unregistrationResponse.getTextPayload();
            string payload = result is string ? result : "";
            map errorDetail = { message : "Error occured during topic unregistration: " + payload };
            error webSubError = error(WEBSUB_ERROR_CODE, errorDetail);
            return webSubError;
        }
    } else if (unregistrationResponse is error) {
        string errCause = <string> unregistrationResponse.detail().message;
        map errorDetail = { message : "Error sending topic unregistration request: " + errCause };
        error webSubError = error(WEBSUB_ERROR_CODE, errorDetail);
        return webSubError;
    }
    return;
}

function CallerActions.publishUpdate(string topic, string|xml|json|byte[]|io:ReadableByteChannel payload,
                                      string? contentType = (), map<string>? headers = ()) returns error? {

    endpoint http:Client httpClientEndpoint = self.httpClientEndpoint;
    http:Request request = new;
    string queryParams = HUB_MODE + "=" + MODE_PUBLISH + "&" + HUB_TOPIC + "=" + topic;
    request.setPayload(payload);

    if (contentType is string) {
        check request.setContentType(contentType);
    }

    if (headers is map<string>) {
        foreach key, value in headers {
            request.setHeader(key, value);
        }
    }

    var response = httpClientEndpoint->post(untaint ("?" + queryParams), request);
    if (response is http:Response) {
        if (!isSuccessStatusCode(response.statusCode)) {
            var result = response.getTextPayload();
            string textPayload = result is string ? result : "";
            map errorDetail = { message : "Error occured publishing update: " + textPayload };
            error webSubError = error(WEBSUB_ERROR_CODE, errorDetail);
            return webSubError;
        }
    } else if (response is error) {
        map errorDetail = { message : "Publish failed for topic [" + topic + "]" };
        error webSubError = error(WEBSUB_ERROR_CODE, errorDetail);
        return webSubError;
    }
    return;
}

function CallerActions.notifyUpdate(string topic, map<string>? headers = ()) returns error? {
    endpoint http:Client httpClientEndpoint = self.httpClientEndpoint;
    http:Request request = new;
    string queryParams = HUB_MODE + "=" + MODE_PUBLISH + "&" + HUB_TOPIC + "=" + topic;

    if (headers is map<string>) {
        foreach key, value in headers {
            request.setHeader(key, value);
        }
    }

    var response = httpClientEndpoint->post(untaint ("?" + queryParams), request);
    if (response is http:Response) {
        if (!isSuccessStatusCode(response.statusCode)) {
            var result = response.getTextPayload();
            string textPayload = result is string ? result : "";
            map errorDetail = { message : "Error occured notifying update availability: " + textPayload };
            error webSubError = error(WEBSUB_ERROR_CODE, errorDetail);
            return webSubError;
        }
    } else if (response is error) {
        map errorDetail = { message : "Update availability notification failed for topic [" + topic + "]" };
        error webSubError = error(WEBSUB_ERROR_CODE, errorDetail);
        return webSubError;
    }
    return;
}

# Builds the topic registration change request to register or unregister a topic at the hub.
#
# + mode - Whether the request is for registration or unregistration
# + topic - The topic to register/unregister
# + return - `http:Request` The Request to send to the hub to register/unregister
function buildTopicRegistrationChangeRequest(@sensitive string mode, @sensitive string topic) returns (http:Request) {
    http:Request request = new;
    request.setTextPayload(HUB_MODE + "=" + mode + "&" + HUB_TOPIC + "=" + topic);
    request.setHeader(CONTENT_TYPE, mime:APPLICATION_FORM_URLENCODED);
    return request;
}

# Function to build the subscription request to subscribe at the hub.
#
# + mode - Whether the request is for subscription or unsubscription
# + subscriptionChangeRequest - The SubscriptionChangeRequest specifying the topic to subscribe to and the
#                               parameters to use
# + return - `http:Request` The Request to send to the hub to subscribe/unsubscribe
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

# Function to process the response from the hub on subscription/unsubscription and extract required information.
#
# + hub - The hub to which the subscription/unsubscription request was sent
# + mode - Whether the request was sent for subscription or unsubscription
# + subscriptionChangeRequest - The subscription change request sent
# + response - The http:Response or error received on request to the hub
# + httpClientEndpoint - The underlying HTTP Client Endpoint
# + return - `SubscriptionChangeResponse` indicating subscription/unsubscription details, if the request was successful
#            else `error` if an error occurred
function processHubResponse(@sensitive string hub, @sensitive string mode,
                            SubscriptionChangeRequest subscriptionChangeRequest,
                            http:Response|error response, http:Client httpClientEndpoint,
                            int remainingRedirects) returns @tainted SubscriptionChangeResponse|error {

    string topic = subscriptionChangeRequest.topic;
    if (response is error) {
        string errCause = <string> response.detail().message;
        map errorDetail = { message : "Error occurred for request: Mode[" + mode + "] at Hub[" + hub + "] - " +
                                errCause };
        error webSubError = error(WEBSUB_ERROR_CODE, errorDetail);
        return webSubError;
    } else if (response is http:Response) {
        int responseStatusCode = response.statusCode;
        if (responseStatusCode == http:TEMPORARY_REDIRECT_307
                || responseStatusCode == http:PERMANENT_REDIRECT_308) {
            if (remainingRedirects > 0) {
                string redirected_hub = response.getHeader("Location");
                return invokeClientConnectorOnRedirection(redirected_hub, mode, subscriptionChangeRequest,
                                                            httpClientEndpoint.config.auth, remainingRedirects - 1);
            }
            map errorDetail = { message : "Redirection response received for subscription change request"
                                    + " made with followRedirects disabled or after maxCount exceeded: Hub ["
                                    + hub + "], Topic [" + subscriptionChangeRequest.topic + "]" };
            error subscriptionError = error(WEBSUB_ERROR_CODE, errorDetail);
            return subscriptionError;
        } else if (!isSuccessStatusCode(responseStatusCode)) {
            var responsePayload = response.getTextPayload();
            string errorMessage = "Error in request: Mode[" + mode + "] at Hub[" + hub + "]";
            if (responsePayload is string) {
                errorMessage = errorMessage + " - " + responsePayload;
            } else if responsePayload is error {
                string errCause = <string> responsePayload.detail().message;
                errorMessage = errorMessage + " - Error occurred identifying cause: " + errCause;
            }
            error webSubError = error(WEBSUB_ERROR_CODE, { message : errorMessage });
            return webSubError;
        } else {
            if (responseStatusCode != http:ACCEPTED_202) {
                log:printDebug("Subscription request considered successful for non 202 status code: "
                                + responseStatusCode);
            }
            SubscriptionChangeResponse subscriptionChangeResponse = {hub:hub, topic:topic, response:response};
            return subscriptionChangeResponse;
        }
    } else {
        error webSubError = error(WEBSUB_ERROR_CODE);
        return webSubError;
    }
}

# Function to invoke the WebSubSubscriberConnector's actions for subscription/unsubscription on redirection from the
# original hub.
#
# + hub - The hub to which the subscription/unsubscription request is to be sent
# + mode - Whether the request is for subscription or unsubscription
# + subscriptionChangeRequest - The request containing subscription/unsubscription details
# + auth - The auth config to use at the hub, if specified
# + return - `SubscriptionChangeResponse` indicating subscription/unsubscription details, if the request was successful
#            else `error` if an error occurred
function invokeClientConnectorOnRedirection(@sensitive string hub, @sensitive string mode, SubscriptionChangeRequest
                                            subscriptionChangeRequest, http:AuthConfig? auth, int remainingRedirects)
    returns @tainted SubscriptionChangeResponse|error {

    if (mode == MODE_SUBSCRIBE) {
        return subscribeWithRetries(hub, subscriptionChangeRequest, auth, remainingRedirects = remainingRedirects);
    }
    return unsubscribeWithRetries(hub, subscriptionChangeRequest, auth, remainingRedirects = remainingRedirects);
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
    if (followRedirects is http:FollowRedirects) {
        if (followRedirects.enabled) {
            return followRedirects.maxCount;
        }
    }
    return 0;
}
