// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/encoding;
import ballerina/http;
import ballerina/log;
import ballerina/mime;

# The HTTP based client for WebSub subscription and unsubscription.
public type SubscriptionClient client object {

    private string url;
    private http:Client httpClient;
    private http:FollowRedirects? followRedirects = ();

    # Initializer function for the client.
    #
    # + url    - The URL to change subscription at
    # + config - The `http:ClientConfiguration` for the underlying client or `()`
    public function __init(string url, http:ClientConfiguration? config = ()) {
        self.url = url;
        self.httpClient = new (self.url, config);
        self.followRedirects = config?.followRedirects;
    }

    # Sends a subscription request to a WebSub Hub.
    #
    # + subscriptionRequest - The `SubscriptionChangeRequest` containing subscription details
    # + return - `SubscriptionChangeResponse` indicating subscription details, if the request was successful else
    #            `error` if an error occurred with the subscription request
    public remote function subscribe(SubscriptionChangeRequest subscriptionRequest)
        returns @tainted SubscriptionChangeResponse|error {

        http:Client httpClient = self.httpClient;
        http:Request builtSubscriptionRequest = buildSubscriptionChangeRequest(MODE_SUBSCRIBE, subscriptionRequest);
        var response = httpClient->post("", builtSubscriptionRequest);
        int redirectCount = getRedirectionMaxCount(self.followRedirects);
        return processHubResponse(self.url, MODE_SUBSCRIBE, subscriptionRequest, response, httpClient,
                                  redirectCount);
    }

    # Sends an unsubscription request to a WebSub Hub.
    #
    # + unsubscriptionRequest - The `SubscriptionChangeRequest` containing unsubscription details
    # + return - `SubscriptionChangeResponse` indicating unsubscription details, if the request was successful else
    #            `error` if an error occurred with the unsubscription request
    public remote function unsubscribe(SubscriptionChangeRequest unsubscriptionRequest)
        returns @tainted SubscriptionChangeResponse|error {

        http:Client httpClient = self.httpClient;
        http:Request builtUnsubscriptionRequest = buildSubscriptionChangeRequest(MODE_UNSUBSCRIBE, unsubscriptionRequest);
        var response = httpClient->post("", builtUnsubscriptionRequest);
        int redirectCount = getRedirectionMaxCount(self.followRedirects);
        return processHubResponse(self.url, MODE_UNSUBSCRIBE, unsubscriptionRequest, response, httpClient,
                                  redirectCount);
    }

};

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
    var encodedCallback = encoding:encodeUriComponent(callback, "UTF-8");
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
# + httpClient - The underlying HTTP Client Endpoint
# + return - `SubscriptionChangeResponse` indicating subscription/unsubscription details, if the request was successful
#            else `error` if an error occurred
function processHubResponse(@untainted string hub, @untainted string mode,
                            SubscriptionChangeRequest subscriptionChangeRequest,
                            http:Response|error response, http:Client httpClient,
                            int remainingRedirects) returns @tainted SubscriptionChangeResponse|error {

    string topic = subscriptionChangeRequest.topic;
    if (response is error) {
        string errCause = <string> response.detail()?.message;
        error webSubError = error(WEBSUB_ERROR_CODE, message = "Error occurred for request: Mode[" + mode
                                        + "] at Hub[" + hub + "] - " + errCause );
        return webSubError;
    } else {
        int responseStatusCode = response.statusCode;
        if (responseStatusCode == http:STATUS_TEMPORARY_REDIRECT
                || responseStatusCode == http:STATUS_PERMANENT_REDIRECT) {
            if (remainingRedirects > 0) {
                string redirected_hub = response.getHeader("Location");
                return invokeClientConnectorOnRedirection(redirected_hub, mode, subscriptionChangeRequest,
                                                            httpClient.config.auth, remainingRedirects - 1);
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
            if (responseStatusCode != http:STATUS_ACCEPTED) {
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

function subscribeWithRetries(string url, SubscriptionChangeRequest subscriptionRequest,
                              http:OutboundAuthConfig? auth, int remainingRedirects = 0)
             returns @tainted SubscriptionChangeResponse| error {
    http:Client clientEndpoint = new http:Client(url, { auth: auth });
    http:Request builtSubscriptionRequest = buildSubscriptionChangeRequest(MODE_SUBSCRIBE, subscriptionRequest);
    var response = clientEndpoint->post("", builtSubscriptionRequest);
    return processHubResponse(url, MODE_SUBSCRIBE, subscriptionRequest, response, clientEndpoint,
                              remainingRedirects);
}

function unsubscribeWithRetries(string url, SubscriptionChangeRequest unsubscriptionRequest,
                                http:OutboundAuthConfig? auth, int remainingRedirects = 0)
             returns @tainted SubscriptionChangeResponse|error {
    http:Client clientEndpoint = new http:Client(url, {
        auth: auth
    });
    http:Request builtSubscriptionRequest = buildSubscriptionChangeRequest(MODE_UNSUBSCRIBE, unsubscriptionRequest);
    var response = clientEndpoint->post("", builtSubscriptionRequest);
    return processHubResponse(url, MODE_UNSUBSCRIBE, unsubscriptionRequest, response, clientEndpoint,
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
