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

@Description {value:"HTTP client connector for outbound WebSub Subscription/Unsubscription requests to a Hub"}
public type HubClientConnector object {

    public {
        string hubUrl;
    }

    private {
        http:Client httpClientEndpoint;
    }

    new (hubUrl, httpClientEndpoint) {}

    @Description {value:"Function to send a subscription request to a WebSub Hub"}
    @Param {value:"subscriptionRequest: The SubscriptionChangeRequest containing subscription details"}
    @Return {value:"SubscriptionChangeResponse indicating subscription details, if the request was successful"}
    @Return {value:"WebSubError if an error occurred with the subscription request"}
    public function subscribe (SubscriptionChangeRequest subscriptionRequest) returns @untainted
                                                                            (SubscriptionChangeResponse | WebSubError);

    @Description {value:"Function to send an unsubscription request to a WebSub Hub"}
    @Param {value:"unsubscriptionRequest: The SubscriptionChangeRequest containing unsubscription details"}
    @Return {value:"SubscriptionChangeResponse indicating unsubscription details, if the request was successful"}
    @Return {value:"WebSubError if an error occurred with the unsubscription request"}
    public function unsubscribe (SubscriptionChangeRequest unsubscriptionRequest) returns @untainted
                                                                            (SubscriptionChangeResponse | WebSubError);

    @Description {value:"Function to register a topic in a Ballerina WebSub Hub against which subscribers can subscribe
     and the publisher will publish updates, with a secret which will be used in signature generation if specified"}
    @Param {value:"topic: The topic to register"}
    @Param {value:"secret: The secret the publisher will use to generate a signature when publishing updates"}
    @Return {value:"WebSubError if an error occurred registering the topic"}
    public function registerTopic (string topic, string secret = "") returns (WebSubError | ());

    @Description {value:"Function to unregister a topic in a Ballerina WebSub Hub"}
    @Param {value:"topic: The topic to unregister"}
    @Param {value:"secret: The secret the publisher used when registering the topic"}
    @Return {value:"WebSubError if an error occurred unregistering the topic"}
    public function unregisterTopic (string topic, string secret = "") returns (WebSubError | ());

    @Description {value:"Function to publish an update to a remote Ballerina WebSub Hub"}
    @Param {value:"topic: The topic for which the update occurred"}
    @Param {value:"payload: The update payload"}
    @Param {value:"secret: The secret used when registering the topic"}
    @Param {value:"signatureMethod: The signature method to use to generate a secret"}
    @Return {value:"WebSubError if an error occurred with the update"}
    public function publishUpdate (string topic, json payload, string secret = "", string signatureMethod = "sha256",
                                                                    json... headers) returns (WebSubError | ());

};

public function HubClientConnector::subscribe (SubscriptionChangeRequest subscriptionRequest) returns
@untainted (SubscriptionChangeResponse | WebSubError) {
    endpoint http:Client httpClientEndpoint = self.httpClientEndpoint;
    http:Request builtSubscriptionRequest = buildSubscriptionChangeRequest(MODE_SUBSCRIBE, subscriptionRequest);
    var response = httpClientEndpoint -> post("", builtSubscriptionRequest);
    return processHubResponse(self.hubUrl, MODE_SUBSCRIBE, subscriptionRequest, response, httpClientEndpoint);
}

public function HubClientConnector::unsubscribe (SubscriptionChangeRequest unsubscriptionRequest) returns
@untainted (SubscriptionChangeResponse | WebSubError) {
    endpoint http:Client httpClientEndpoint = self.httpClientEndpoint;
    http:Request builtUnsubscriptionRequest = buildSubscriptionChangeRequest(MODE_UNSUBSCRIBE, unsubscriptionRequest);
    var response = httpClientEndpoint -> post("", builtUnsubscriptionRequest);
    return processHubResponse(self.hubUrl, MODE_UNSUBSCRIBE, unsubscriptionRequest, response, httpClientEndpoint);
}

public function HubClientConnector::registerTopic (string topic, string secret = "") returns (WebSubError | ()) {
    endpoint http:Client httpClientEndpoint = self.httpClientEndpoint;
    http:Request request = buildTopicRegistrationChangeRequest(MODE_REGISTER, topic, secret);
    var registrationResponse = httpClientEndpoint -> post("", request);
    match (registrationResponse) {
        http:Response response => {
            if (response.statusCode != http:ACCEPTED_202) {
                string payload = response.getStringPayload() but { http:PayloadError => "" };
                WebSubError webSubError = { message: "Error occured during topic registration: " + payload };
                return webSubError;
            }
            return;
        }
        http:HttpConnectorError err => {
            WebSubError webSubError = { message: "Error sending topic registration request: " + err.message,
                                        cause: err };
            return webSubError;
        }
    }
}

public function HubClientConnector::unregisterTopic (string topic, string secret = "") returns (WebSubError | ()) {
    endpoint http:Client httpClientEndpoint = self.httpClientEndpoint;
    http:Request request = buildTopicRegistrationChangeRequest(MODE_UNREGISTER, topic, secret);
    var unregistrationResponse = httpClientEndpoint -> post("", request);
    match (unregistrationResponse) {
        http:Response response => {
            if (response.statusCode != http:ACCEPTED_202) {
                string payload = response.getStringPayload() but { http:PayloadError => "" };
                WebSubError webSubError = { message: "Error occured during topic unregistration: " + payload };
                return webSubError;
            }
            return;
        }
        http:HttpConnectorError err => {
            WebSubError webSubError = { message: "Error sending topic unregistration request: " + err.message,
                cause: err };
            return webSubError;
        }
    }
}

public function HubClientConnector::publishUpdate (string topic, json payload,
            string secret = "", string signatureMethod = "sha256", json... headers) returns (WebSubError | ()) {
    endpoint http:Client httpClientEndpoint = self.httpClientEndpoint;
    http:Request request = new;
    string queryParams = HUB_MODE + "=" + MODE_PUBLISH + "&" + HUB_TOPIC + "=" + topic;
    request.setJsonPayload(payload);

    if (secret != "") {
        string stringPayload = payload.toString() but { () => "" };
        string publisherSignature = signatureMethod + "=";
        string generatedSignature = "";
        if (SHA1.equalsIgnoreCase(signatureMethod)) {
            generatedSignature = crypto:getHmac(stringPayload, secret, crypto:SHA1);
        } else if (SHA256.equalsIgnoreCase(signatureMethod)) {
            generatedSignature = crypto:getHmac(stringPayload, secret, crypto:SHA256);
        } else if (MD5.equalsIgnoreCase(signatureMethod)) {
            generatedSignature = crypto:getHmac(stringPayload, secret, crypto:MD5);
        }
        publisherSignature = publisherSignature + generatedSignature;
        request.setHeader(PUBLISHER_SIGNATURE, publisherSignature);
    }

    foreach headerJson in headers {
        string strHeaderKey = headerJson.headerKey.toString() but { () => "" };
        string strHeaderValue = headerJson.headerValue.toString() but { () => "" };
        request.setHeader(strHeaderKey, strHeaderValue);
    }

    var response = httpClientEndpoint -> post("?" + queryParams, request);
        match (response) {
            http:Response => return;
            http:HttpConnectorError httpConnectorError => { WebSubError webSubError = {
                      message:"Notification failed for topic [" + topic + "]", cause:httpConnectorError };
                                                            return webSubError;
            }
    }
}

@Description {value:"Function to build the topic registration change request to rgister/unregister a topic at the hub"}
@Param {value:"mode: Whether the request is for registration or unregistration"}
@Param {value:"subscriptionChangeRequest: The SubscriptionChangeRequest specifying the topic to subscribe to and the
                                        parameters to use"}
@Return {value:"The Request to send to the hub to subscribe/unsubscribe"}
function buildTopicRegistrationChangeRequest(string mode, string topic, string secret) returns (http:Request) {
    http:Request request = new;
    string body = HUB_MODE + "=" + mode + "&" + HUB_TOPIC + "=" + topic;
    if (secret != "") {
        body = body + "&" + PUBLISHER_SECRET + "=" + secret;
    }
    request.setStringPayload(body);
    request.setHeader(CONTENT_TYPE, mime:APPLICATION_FORM_URLENCODED);
    return request;
}

@Description {value:"Function to build the subscription request to subscribe at the hub"}
@Param {value:"mode: Whether the request is for subscription or unsubscription"}
@Param {value:"subscriptionChangeRequest: The SubscriptionChangeRequest specifying the topic to subscribe to and the
                                        parameters to use"}
@Return {value:"The Request to send to the hub to subscribe/unsubscribe"}
function buildSubscriptionChangeRequest(string mode, SubscriptionChangeRequest subscriptionChangeRequest) returns
(http:Request) {
    http:Request request = new;
    string body = HUB_MODE + "=" + mode
                  + "&" + HUB_TOPIC + "=" + subscriptionChangeRequest.topic
                  + "&" + HUB_CALLBACK + "=" + subscriptionChangeRequest.callback;
    if (mode == MODE_SUBSCRIBE) {
        body = body + "&" + HUB_SECRET + "=" + subscriptionChangeRequest.secret + "&" + HUB_LEASE_SECONDS + "="
               + subscriptionChangeRequest.leaseSeconds;
    }
    request.setStringPayload(body);
    request.setHeader(CONTENT_TYPE, mime:APPLICATION_FORM_URLENCODED);
    return request;
}

@Description {value:"Function to process the response from the hub on subscription/unsubscription and extract
                    required information"}
@Param {value:"hub: The hub to which the subscription/unsubscription request was sent"}
@Param {value:"mode: Whether the request was sent for subscription or unsubscription"}
@Param {value:"topic: The topic for which the subscription/unsubscription request was sent"}
@Param {value:"response: The response received from the hub"}
@Param {value:"httpConnectorError: Error, if occurred, with HTTP client connector invocation"}
@Return {value:"SubscriptionChangeResponse including details of subscription/unsubscription,
                if the request was successful"}
@Return { value : "WebSubErrror indicating any errors that occurred, if the request was unsuccessful"}
function processHubResponse(string hub, string mode, SubscriptionChangeRequest subscriptionChangeRequest,
                            http:Response|http:HttpConnectorError response, http:Client httpClientEndpoint)
                            returns @untainted (SubscriptionChangeResponse | WebSubError) {
    string topic = subscriptionChangeRequest.topic;
    match response {
        http:HttpConnectorError httpConnectorError => {
            string errorMessage = "Error occurred for request: Mode[" + mode + "] at Hub[" + hub +"] - "
                                        + httpConnectorError.message;
            WebSubError webSubError = { message:errorMessage, cause:httpConnectorError };
            return webSubError;
        }
        http:Response httpResponse => {
            int responseStatusCode = httpResponse.statusCode;
            if (responseStatusCode == http:TEMPORARY_REDIRECT_307 || responseStatusCode == 308) {
                string redirected_hub = httpResponse.getHeader("Location");
                return invokeClientConnectorOnRedirection(redirected_hub, mode, subscriptionChangeRequest);
            } else if (responseStatusCode != http:ACCEPTED_202) {
                var responsePayload = httpResponse.getStringPayload();
                string errorMessage = "Error in request: Mode[" + mode + "] at Hub[" + hub +"]";
                match (responsePayload) {
                    string responseErrorPayload => { errorMessage = errorMessage + " - " + responseErrorPayload; }
                    http:PayloadError payloadError => { errorMessage = errorMessage + " - "
                                                                       + "Error occurred identifying"
                                                                       + "cause: " + payloadError.message; }
                }
                WebSubError webSubError = { message:errorMessage };
                return webSubError;
            } else {
                SubscriptionChangeResponse subscriptionChangeResponse = {hub:hub, topic:topic, response:httpResponse};
                return subscriptionChangeResponse;
            }
        }
    }
}

@Description {value:"Function to invoke the WebSubSubscriberConnector's actions for subscription/unsubscription on
redirection from original hub"}
@Param {value:"hub: The hub to which the subscription/unsubscription request is to be sent"}
@Param {value:"mode: Whether the request is for subscription or unsubscription"}
@Param {value:"subscriptionChangeRequest: The request containing subscription details"}
function invokeClientConnectorOnRedirection (string hub, string mode,
SubscriptionChangeRequest subscriptionChangeRequest) returns @untainted  (SubscriptionChangeResponse | WebSubError) {
    endpoint Client websubHubClientEP { url:hub };
    if (mode == MODE_SUBSCRIBE) {
        var response = websubHubClientEP -> subscribe(subscriptionChangeRequest);
        return response;
    } else {
        var response = websubHubClientEP -> unsubscribe(subscriptionChangeRequest);
        return response;
    }
}

