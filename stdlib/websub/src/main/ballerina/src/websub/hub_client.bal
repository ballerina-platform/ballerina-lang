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

# The HTTP based Caller remote functions for outbound WebSub Subscription, Unsubscription, Registration, Unregistration and
# Notification requests to a Hub.
#
# + hubUrl - The URL of the target Hub to which requests need to be sent
public type Client client object {

    public string hubUrl;

    private http:Client httpClientEndpoint;
    private http:FollowRedirects? followRedirects = ();

    public function __init(string url, http:ClientEndpointConfig? config = ()) {
        self.hubUrl = url;
        self.httpClientEndpoint = new (self.hubUrl, config);
        self.followRedirects = config?.followRedirects;
    }

    # Sends a subscription request to a WebSub Hub.
    #
    # + subscriptionRequest - The `SubscriptionChangeRequest` containing subscription details
    # + return - `SubscriptionChangeResponse` indicating subscription details, if the request was successful else
    #            `error` if an error occurred with the subscription request
    public remote function subscribe(SubscriptionChangeRequest subscriptionRequest)
        returns @tainted SubscriptionChangeResponse|error {

        http:Client httpClientEndpoint = self.httpClientEndpoint;
        http:Request builtSubscriptionRequest = buildSubscriptionChangeRequest(MODE_SUBSCRIBE, subscriptionRequest);
        var response = httpClientEndpoint->post("", builtSubscriptionRequest);
        int redirectCount = getRedirectionMaxCount(self.followRedirects);
        return processHubResponse(self.hubUrl, MODE_SUBSCRIBE, subscriptionRequest, response, httpClientEndpoint,
                                  redirectCount);
    }

    # Sends an unsubscription request to a WebSub Hub.
    #
    # + unsubscriptionRequest - The `SubscriptionChangeRequest` containing unsubscription details
    # + return - `SubscriptionChangeResponse` indicating unsubscription details, if the request was successful else
    #            `error` if an error occurred with the unsubscription request
    public remote function unsubscribe(SubscriptionChangeRequest unsubscriptionRequest)
        returns @tainted SubscriptionChangeResponse|error {

        http:Client httpClientEndpoint = self.httpClientEndpoint;
        http:Request builtUnsubscriptionRequest = buildSubscriptionChangeRequest(MODE_UNSUBSCRIBE, unsubscriptionRequest);
        var response = httpClientEndpoint->post("", builtUnsubscriptionRequest);
        int redirectCount = getRedirectionMaxCount(self.followRedirects);
        return processHubResponse(self.hubUrl, MODE_UNSUBSCRIBE, unsubscriptionRequest, response, httpClientEndpoint,
                                  redirectCount);
    }

    # Registers a topic in a Ballerina WebSub Hub against which subscribers can subscribe and the publisher will
    # publish updates.
    #
    # + topic - The topic to register
    # + return - `error` if an error occurred registering the topic
    public remote function registerTopic(string topic) returns @tainted error? {
        http:Client httpClientEndpoint = self.httpClientEndpoint;
        http:Request request = buildTopicRegistrationChangeRequest(MODE_REGISTER, topic);
        var registrationResponse = httpClientEndpoint->post("", request);
        if (registrationResponse is http:Response) {
            if (registrationResponse.statusCode != http:ACCEPTED_202) {
                var result = registrationResponse.getTextPayload();
                string payload = result is string ? result : "";
                error webSubError = error(WEBSUB_ERROR_CODE, message = "Error occurred during topic registration: " + payload);
                return webSubError;
            }
        } else {
            error err = registrationResponse;
            string errCause = <string> err.detail()?.message;
            error webSubError = error(WEBSUB_ERROR_CODE, message = "Error sending topic registration request: " + errCause);
            return webSubError;
        }
    }

    # Unregisters a topic in a Ballerina WebSub Hub.
    #
    # + topic - The topic to unregister
    # + return - `error` if an error occurred unregistering the topic
    public remote function unregisterTopic(string topic) returns @tainted error? {
        http:Client httpClientEndpoint = self.httpClientEndpoint;
        http:Request request = buildTopicRegistrationChangeRequest(MODE_UNREGISTER, topic);
        var unregistrationResponse = httpClientEndpoint->post("", request);
        if (unregistrationResponse is http:Response) {
            if (unregistrationResponse.statusCode != http:ACCEPTED_202) {
                var result = unregistrationResponse.getTextPayload();
                string payload = result is string ? result : "";
                error webSubError = error(WEBSUB_ERROR_CODE, message = "Error occurred during topic unregistration: " + payload);
                return webSubError;
            }
        } else {
            error err = unregistrationResponse;
            string errCause = <string> err.detail()?.message;
            error webSubError = error(WEBSUB_ERROR_CODE, message = "Error sending topic unregistration request: " + errCause);
            return webSubError;
        }
        return;
    }

    # Publishes an update to a remote Ballerina WebSub Hub.
    #
    # + topic - The topic for which the update occurred
    # + payload - The update payload
    # + contentType - The type of the update content, to set as the `ContentType` header
    # + headers - The headers, if any, that need to be set
    # + return - `error` if an error occurred with the update
    public remote function publishUpdate(string topic, string|xml|json|byte[]|io:ReadableByteChannel payload,
                                         string? contentType = (), map<string>? headers = ()) returns @tainted error? {
        http:Client httpClientEndpoint = self.httpClientEndpoint;
        http:Request request = new;
        string queryParams = HUB_MODE + "=" + MODE_PUBLISH + "&" + HUB_TOPIC + "=" + topic;
        request.setPayload(payload);

        if (contentType is string) {
            check request.setContentType(contentType);
        }

        if (headers is map<string>) {
            foreach var [key, value] in headers.entries() {
                request.setHeader(key, value);
            }
        }

        var response = httpClientEndpoint->post(<@untainted string> ("?" + queryParams), request);
        if (response is http:Response) {
            if (!isSuccessStatusCode(response.statusCode)) {
                var result = response.getTextPayload();
                string textPayload = result is string ? result : "";
                error webSubError = error(WEBSUB_ERROR_CODE, message = "Error occurred publishing update: " + textPayload);
                return webSubError;
            }
        } else {
            error webSubError = error(WEBSUB_ERROR_CODE, message = "Publish failed for topic [" + topic + "]");
            return webSubError;
        }
        return;
    }

    # Notifies a remote WebSub Hub that an update is available to fetch, for hubs that require publishing to
    # happen as such.
    #
    # + topic - The topic for which the update occurred
    # + headers - The headers, if any, that need to be set
    # + return - `error` if an error occurred with the notification
    public remote function notifyUpdate(string topic, map<string>? headers = ()) returns @tainted error? {
        http:Client httpClientEndpoint = self.httpClientEndpoint;
        http:Request request = new;
        string queryParams = HUB_MODE + "=" + MODE_PUBLISH + "&" + HUB_TOPIC + "=" + topic;

        if (headers is map<string>) {
            foreach var [key, value] in headers.entries() {
                request.setHeader(key, value);
            }
        }

        var response = httpClientEndpoint->post(<@untainted string> ("?" + queryParams), request);
        if (response is http:Response) {
            if (!isSuccessStatusCode(response.statusCode)) {
                var result = response.getTextPayload();
                string textPayload = result is string ? result : "";
                error webSubError = error(WEBSUB_ERROR_CODE,
                                            message = "Error occurred notifying update availability: " + textPayload);
                return webSubError;
            }
        } else {
            error webSubError = error(WEBSUB_ERROR_CODE,
                                        message = "Update availability notification failed for topic [" + topic + "]");
            return webSubError;
        }
        return;
    }
};

# Builds the topic registration change request to register or unregister a topic at the hub.
#
# + mode - Whether the request is for registration or unregistration
# + topic - The topic to register/unregister
# + return - `http:Request` The Request to send to the hub to register/unregister
function buildTopicRegistrationChangeRequest(@untainted string mode, @untainted string topic) returns (http:Request) {
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
function buildSubscriptionChangeRequest(@untainted string mode,
                                        SubscriptionChangeRequest subscriptionChangeRequest) returns (http:Request) {
    http:Request request = new;

    string callback = subscriptionChangeRequest.callback;
    var encodedCallback = http:encode(callback, "UTF-8");
    if (encodedCallback is string) {
        callback = encodedCallback;
    }

    string body = HUB_MODE + "=" + mode
        + "&" + HUB_TOPIC + "=" + subscriptionChangeRequest.topic
        + "&" + HUB_CALLBACK + "=" + callback;
    if (mode == MODE_SUBSCRIBE) {
        if (subscriptionChangeRequest.secret.trim() != "") {
            body = body + "&" + HUB_SECRET + "=" + subscriptionChangeRequest.secret;
        }
        if (subscriptionChangeRequest.leaseSeconds != 0) {
            body = body + "&" + HUB_LEASE_SECONDS + "=" + subscriptionChangeRequest.leaseSeconds.toString();
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
function processHubResponse(@untainted string hub, @untainted string mode,
                            SubscriptionChangeRequest subscriptionChangeRequest,
                            http:Response|error response, http:Client httpClientEndpoint,
                            int remainingRedirects) returns @tainted SubscriptionChangeResponse|error {

    string topic = subscriptionChangeRequest.topic;
    if (response is error) {
        string errCause = <string> response.detail()?.message;
        error webSubError = error(WEBSUB_ERROR_CODE, message = "Error occurred for request: Mode[" + mode
                                        + "] at Hub[" + hub + "] - " + errCause );
        return webSubError;
    } else {
        int responseStatusCode = response.statusCode;
        if (responseStatusCode == http:TEMPORARY_REDIRECT_307
                || responseStatusCode == http:PERMANENT_REDIRECT_308) {
            if (remainingRedirects > 0) {
                string redirected_hub = response.getHeader("Location");
                return invokeClientConnectorOnRedirection(redirected_hub, mode, subscriptionChangeRequest,
                                                            httpClientEndpoint.config.auth, remainingRedirects - 1);
            }
            error subscriptionError = error(WEBSUB_ERROR_CODE, message = "Redirection response received for "
                    + "subscription change request made with followRedirects disabled or after maxCount exceeded: Hub ["
                    + hub + "], Topic [" + subscriptionChangeRequest.topic + "]");
            return subscriptionError;
        } else if (!isSuccessStatusCode(responseStatusCode)) {
            var responsePayload = response.getTextPayload();
            string errorMessage = "Error in request: Mode[" + mode + "] at Hub[" + hub + "]";
            if (responsePayload is string) {
                errorMessage = errorMessage + " - " + responsePayload;
            } else {
                error err = responsePayload;
                string errCause = <string> err.detail()?.message;
                errorMessage = errorMessage + " - Error occurred identifying cause: " + errCause;
            }
            error webSubError = error(WEBSUB_ERROR_CODE, message = errorMessage);
            return webSubError;
        } else {
            if (responseStatusCode != http:ACCEPTED_202) {
                log:printDebug("Subscription request considered successful for non 202 status code: "
                                + responseStatusCode.toString());
            }
            SubscriptionChangeResponse subscriptionChangeResponse = {hub:hub, topic:topic, response:response};
            return subscriptionChangeResponse;
        }
    }
}

# Function to invoke the WebSubSubscriberConnector's remote functions for subscription/unsubscription on redirection from the
# original hub.
#
# + hub - The hub to which the subscription/unsubscription request is to be sent
# + mode - Whether the request is for subscription or unsubscription
# + subscriptionChangeRequest - The request containing subscription/unsubscription details
# + auth - The auth config to use at the hub, if specified
# + return - `SubscriptionChangeResponse` indicating subscription/unsubscription details, if the request was successful
#            else `error` if an error occurred
function invokeClientConnectorOnRedirection(@untainted string hub, @untainted string mode,
                                            SubscriptionChangeRequest subscriptionChangeRequest,
                                            http:OutboundAuthConfig? auth, int remainingRedirects)
    returns @tainted SubscriptionChangeResponse|error {

    if (mode == MODE_SUBSCRIBE) {
        return subscribeWithRetries(hub, subscriptionChangeRequest, auth, remainingRedirects = remainingRedirects);
    }
    return unsubscribeWithRetries(hub, subscriptionChangeRequest, auth, remainingRedirects = remainingRedirects);
}

function subscribeWithRetries(string hubUrl, SubscriptionChangeRequest subscriptionRequest,
                              http:OutboundAuthConfig? auth, int remainingRedirects = 0)
             returns @tainted SubscriptionChangeResponse| error {
    http:Client clientEndpoint = new http:Client(hubUrl, { auth: auth });
    http:Request builtSubscriptionRequest = buildSubscriptionChangeRequest(MODE_SUBSCRIBE, subscriptionRequest);
    var response = clientEndpoint->post("", builtSubscriptionRequest);
    return processHubResponse(hubUrl, MODE_SUBSCRIBE, subscriptionRequest, response, clientEndpoint,
                              remainingRedirects);
}

function unsubscribeWithRetries(string hubUrl, SubscriptionChangeRequest unsubscriptionRequest,
                                http:OutboundAuthConfig? auth, int remainingRedirects = 0)
             returns @tainted SubscriptionChangeResponse|error {
    http:Client clientEndpoint = new http:Client(hubUrl, {
        auth: auth
    });
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
