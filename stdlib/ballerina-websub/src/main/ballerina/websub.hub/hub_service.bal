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
package ballerina.websub.hub;

import ballerina/sql;
import ballerina/http;
import ballerina/log;
import ballerina/mime;
import ballerina/security.crypto;
import ballerina/time;
import ballerina/util;
import ballerina/websub;

endpoint http:Listener hubServiceEP {
    host:hubHost,
    port:hubPort,
    secureSocket:serviceSecureSocket
};

map<PendingSubscriptionChangeRequest> pendingRequests;

@http:ServiceConfig {
    basePath:BASE_PATH
}
service<http:Service> hubService bind hubServiceEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:HUB_PATH
    }
    status (endpoint client, http:Request request) {
        http:Response response = new;
        response.statusCode = http:ACCEPTED_202;
        response.setStringPayload("Ballerina Hub Service - Up and Running!");
        _ = client -> respond(response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:HUB_PATH
    }
    hub (endpoint client, http:Request request) {
        http:Response response = new;
        string mode;
        string topic;

        map params = request.getFormParams() but { mime:EntityError => {} };

        if (params.hasKey(websub:HUB_MODE)) {
            mode = <string> params[websub:HUB_MODE];
        }

        if (params.hasKey(websub:HUB_TOPIC)) {
            string topicFromParams = <string> params[websub:HUB_TOPIC];
            topic = http:decode(topicFromParams, "UTF-8") but { error => topicFromParams };
        }

        if (mode == websub:MODE_SUBSCRIBE || mode == websub:MODE_UNSUBSCRIBE) {
            boolean validSubscriptionRequest = false;
            string callbackFromParams = <string> params[websub:HUB_CALLBACK];
            string callback = http:decode(callbackFromParams, "UTF-8") but { error => callbackFromParams };
            match (validateSubscriptionChangeRequest(mode, topic, callback)) {
                string errorMessage => {
                    response.statusCode = http:BAD_REQUEST_400;
                    response.setStringPayload(errorMessage);
                }
                boolean => {
                    validSubscriptionRequest = true;
                    response.statusCode = http:ACCEPTED_202;
                }
            }
            _ = client -> respond(response);
            if (validSubscriptionRequest) {
                verifyIntent(callback, topic, params);
            }
            done;
        } else if (mode == websub:MODE_REGISTER) {
            if (!hubRemotePublishingEnabled || !hubTopicRegistrationRequired) {
                response.statusCode = http:BAD_REQUEST_400;
                response.setStringPayload("Remote topic registration not allowed/not required at the Hub");
                log:printWarn("Remote topic registration denied at Hub");
                _ = client -> respond(response);
                done;
            }

            string secret = "";
            if (params.hasKey(websub:PUBLISHER_SECRET)) {
                secret = <string> params[websub:PUBLISHER_SECRET];
            }
            string errorMessage = websub:registerTopicAtHub(topic, secret);
            if (errorMessage != "") {
                response.statusCode = http:BAD_REQUEST_400;
                response.setStringPayload(errorMessage);
                log:printWarn("Topic registration unsuccessful at Hub for Topic[" + topic + "]: " + errorMessage);
            } else {
                response.statusCode = http:ACCEPTED_202;
                log:printInfo("Topic registration successful at Hub, for topic[" + topic + "]");
            }
            _ = client -> respond(response);
        } else if (mode == websub:MODE_UNREGISTER) {
            if (!hubRemotePublishingEnabled || !hubTopicRegistrationRequired) {
                response.statusCode = http:BAD_REQUEST_400;
                response.setStringPayload("Remote unregistration not allowed/not required at the Hub");
                log:printWarn("Remote topic unregistration denied at Hub");
                _ = client -> respond(response);
                done;
            }

            string secret = "";
            if (params.hasKey(websub:PUBLISHER_SECRET)) {
                secret = <string> params[websub:PUBLISHER_SECRET];
            }
            string errorMessage = websub:unregisterTopicAtHub(topic, secret);
            if (errorMessage != "") {
                response.statusCode = http:BAD_REQUEST_400;
                response.setStringPayload(errorMessage);
                log:printWarn("Topic unregistration unsuccessful at Hub for Topic[" + topic + "]: " + errorMessage);
            } else {
                response.statusCode = http:ACCEPTED_202;
                log:printInfo("Topic unregistration successful at Hub, for topic[" + topic + "]");
            }
            _ = client -> respond(response);
        } else {
            if (mode != websub:MODE_PUBLISH) {
                params = request.getQueryParams();
                mode = <string> params[websub:HUB_MODE];
                string topicFromParams = <string> params[websub:HUB_TOPIC];
                topic = http:decode(topicFromParams, "UTF-8") but { error => topicFromParams };
            }

            if (mode == websub:MODE_PUBLISH && hubRemotePublishingEnabled) {
                if (!hubTopicRegistrationRequired || websub:isTopicRegistered(topic)) {
                    var reqJsonPayload = request.getJsonPayload(); //TODO: allow others
                    if (hubRemotePublishingMode == websub:REMOTE_PUBLISHING_MODE_FETCH) {
                        match (fetchTopicUpdate(topic)) {
                            http:Response fetchResp => { reqJsonPayload = fetchResp.getJsonPayload(); }
                            http:HttpConnectorError err => {
                                log:printError("Error fetching updates for topic URL [" + topic + "]: " + err.message);
                                response.statusCode = http:BAD_REQUEST_400;
                                _ = client -> respond(response);
                                done;
                            }
                        }
                    }

                    match (reqJsonPayload) {
                        json payload => {
                            response.statusCode = http:ACCEPTED_202;
                            _ = client -> respond(response);
                            if (hubTopicRegistrationRequired) {
                                string secret = websub:retrievePublisherSecret(topic);
                                if (secret != "") {
                                    if (request.hasHeader(websub:PUBLISHER_SIGNATURE)) {
                                        string publisherSignature = request.getHeader(websub:PUBLISHER_SIGNATURE);
                                        string strPayload = payload.toString() but { () => "" };
                                        var signatureValidation = websub:validateSignature(publisherSignature,
                                                                                                    strPayload, secret);
                                        match (signatureValidation) {
                                            websub:WebSubError err => {
                                                log:printWarn("Signature validation failed for publish request for "
                                                              + "topic[" + topic + "]: " + err.message);
                                                done;
                                            }
                                            () => {
                                                log:printInfo("Signature validation successful for publish request "
                                                      + "for Topic [" + topic + "]");
                                            }
                                        }
                                    }
                                }
                            }
                            string errorMessage = websub:publishToInternalHub(topic, payload);
                            if (errorMessage == "") {
                                log:printInfo("Event notification done for Topic [" + topic + "]");
                            } else {
                                log:printError("Event notification failed for Topic [" + topic + "]: " + errorMessage);
                            }
                            done;
                        }
                        http:PayloadError payloadError => {
                            log:printError("Error retreiving payload for WebSub publish request: "
                                           + payloadError.message);
                        }
                    }
                }
                response.statusCode = http:BAD_REQUEST_400;
                _ = client -> respond(response);
            } else {
                response.statusCode = http:BAD_REQUEST_400;
                _ = client -> respond(response);
            }
        }
    }
}

@Description {value:"Function to validate a subscription/unsubscription request, by validating the mode, topic and
                    callback specified."}
@Param {value:"mode: Mode specified in the subscription change request parameters"}
@Param {value:"topic: Topic specified in the subscription change request parameters"}
@Param {value:"callback: Callback specified in the subscription change request parameters"}
@Return {value:"Whether the subscription/unsubscription request is valid"}
@Return {value:"If invalid, the error with the subscription/unsubscription request"}
function validateSubscriptionChangeRequest(string mode, string topic, string callback) returns (boolean | string) {
    if (topic != "" && callback != "") {
        PendingSubscriptionChangeRequest pendingRequest = new (mode, topic, callback);
        pendingRequests[generateKey(topic, callback)] = pendingRequest;
        if (!callback.hasPrefix("http://") && !callback.hasPrefix("https://")) {
            return "Malformed URL specified as callback";
        }
        if (hubTopicRegistrationRequired && !websub:isTopicRegistered(topic)) {
            return "Subscription request denied for unregistered topic";
        }
        return true;
    }
    return "Topic/Callback cannot be null for subscription/unsubscription request";
}

@Description {value:"Function to initiate intent verification for a valid subscription/unsubscription request received."}
@Param {value:"callback: The callback URL of the new subscription/unsubscription request"}
@Param {value:"params: Parameters specified in the new subscription/unsubscription request"}
function verifyIntent(string callback, string topic, map params) {
    endpoint http:Client callbackEp {
        targets:[{url:callback, secureSocket: secureSocket }]
    };

    string mode = <string> params[websub:HUB_MODE];
    string secret = <string> params[websub:HUB_SECRET];
    int leaseSeconds;

    match (<int> params[websub:HUB_LEASE_SECONDS]) {
        int extrLeaseSeconds => { leaseSeconds = extrLeaseSeconds; }
        error => { leaseSeconds = 0; }
    }

    //measured from the time the verification request was made from the hub to the subscriber from the recommendation
    int createdAt = time:currentTime().time;

    if (!(leaseSeconds > 0)) {
          leaseSeconds = hubLeaseSeconds;
    }
    string challenge = util:uuid();

    http:Request request = new;

    string queryParams = websub:HUB_MODE + "=" + mode
                         + "&" + websub:HUB_TOPIC + "=" + topic
                         + "&" + websub:HUB_CHALLENGE + "=" + challenge
                         + "&" + websub:HUB_LEASE_SECONDS + "=" + leaseSeconds;

    var subscriberResponse = callbackEp -> get(untaint ("?" + queryParams), request);

    match (subscriberResponse) {
        http:Response response => {
            var respStringPayload = response.getStringPayload();
            match (respStringPayload) {
                string payload => {
                    if (payload != challenge) {
                        log:printInfo("Intent verification failed for mode: [" + mode + "], for callback URL: ["
                                                     + callback + "]: Challenge not echoed correctly.");
                    } else {
                        websub:SubscriptionDetails subscriptionDetails = {topic:topic, callback:callback, secret:secret,
                                              leaseSeconds:leaseSeconds, createdAt:createdAt};
                        if (mode == websub:MODE_SUBSCRIBE) {
                            websub:addSubscription(subscriptionDetails);
                        } else {
                            websub:removeSubscription(topic, callback);
                        }

                        if (hubPersistenceEnabled) {
                            changeSubscriptionInDatabase(mode, subscriptionDetails);
                        }
                        log:printInfo("Intent verification successful for mode: [" + mode + "], for callback URL: ["
                                                             + callback + "]");
                    }
                }
                http:PayloadError payloadError => {
                    log:printInfo("Intent verification failed for mode: [" + mode + "], for callback URL: [" + callback
                                  + "]: Error retrieving response payload: " + payloadError.message);
                }
            }
        }
        http:HttpConnectorError httpConnectorError => {
            log:printInfo("Error sending intent verification request for callback URL: [" + callback
                     + "]: " + httpConnectorError.message);
        }
    }
    PendingSubscriptionChangeRequest  pendingSubscriptionChangeRequest = new (mode, topic, callback);
    string key = generateKey(topic, callback);
    if (pendingRequests.hasKey(key)) {
        PendingSubscriptionChangeRequest retrievedRequest = <PendingSubscriptionChangeRequest> pendingRequests[key];
        if (pendingSubscriptionChangeRequest.equals(retrievedRequest)) {
            _ = pendingRequests.remove(key);
        }
    }
}

@Description {value:"Function to add/remove the details of topics registered, in the database"}
@Param {value:"mode: Whether the change is for addition/removal"}
@Param {value:"topic: The topic for which registration is changing"}
@Param {value:"secret: The secret if specified when registering, empty string if not"}
function changeTopicRegistrationInDatabase(string mode, string topic, string secret) {
    endpoint sql:Client subscriptionDbEp {
        url: hubDatabaseUrl,
        username: hubDatabaseUsername,
        password: hubDatabasePassword,
        poolOptions: {maximumPoolSize:5}
    };

    sql:Parameter para1 = (sql:TYPE_VARCHAR, topic);
    sql:Parameter para2 = (sql:TYPE_VARCHAR, secret);
    sql:Parameter[] sqlParams = [para1, para2];
    if (mode == websub:MODE_REGISTER) {
        var updateStatus = subscriptionDbEp -> update("INSERT INTO topics (topic,secret) VALUES (?,?)", sqlParams);
        match (updateStatus) {
            int rowCount => log:printInfo("Successfully updated " + rowCount + " entries for registration");
            error err => log:printError("Error occurred updating registration data: " + err.message);
        }
    } else {
        var updateStatus = subscriptionDbEp -> update("DELETE FROM topics WHERE topic=? AND secret=?",
                                                      sqlParams);
        match (updateStatus) {
            int rowCount => log:printInfo("Successfully updated " + rowCount + " entries for unregistration");
            error err => log:printError("Error occurred updating unregistration data: " + err.message);
        }
    }
    _ = subscriptionDbEp -> close();
}

@Description {value:"Function to add/change/remove the subscription details in the database"}
@Param {value:"mode: Whether the subscription change is for unsubscription/unsubscription"}
@Param {value:"subscriptionDetails: The details of the subscription changing"}
function changeSubscriptionInDatabase(string mode, websub:SubscriptionDetails subscriptionDetails) {
    endpoint sql:Client subscriptionDbEp {
        url: hubDatabaseUrl,
        username: hubDatabaseUsername,
        password: hubDatabasePassword,
        poolOptions: {maximumPoolSize:5}
    };

    sql:Parameter para1 = (sql:TYPE_VARCHAR, subscriptionDetails.topic);
    sql:Parameter para2 = (sql:TYPE_VARCHAR, subscriptionDetails.callback);
    sql:Parameter[] sqlParams;
    if (mode == websub:MODE_SUBSCRIBE) {
        sql:Parameter para3 = (sql:TYPE_VARCHAR, subscriptionDetails.secret);
        sql:Parameter para4 = (sql:TYPE_BIGINT, subscriptionDetails.leaseSeconds);
        sql:Parameter para5 = (sql:TYPE_BIGINT, subscriptionDetails.createdAt);
        sqlParams = [para1, para2, para3, para4, para5, para3, para4, para5];
        var updateStatus = subscriptionDbEp -> update("INSERT INTO subscriptions"
                                             + " (topic,callback,secret,lease_seconds,created_at) VALUES (?,?,?,?,?) ON"
                                             + " DUPLICATE KEY UPDATE secret=?, lease_seconds=?,created_at=?",
                                             sqlParams);
        match (updateStatus) {
            int rowCount => log:printInfo("Successfully updated " + rowCount + " entries for subscription");
            error err => log:printError("Error occurred updating subscription data: " + err.message);
        }
    } else {
        sqlParams = [para1, para2];
        var updateStatus = subscriptionDbEp -> update(
                                               "DELETE FROM subscriptions WHERE topic=? AND callback=?", sqlParams);
        match (updateStatus) {
            int rowCount => log:printInfo("Successfully updated " + rowCount + " entries for unsubscription");
            error err => log:printError("Error occurred updating unsubscription data: " + err.message);
        }
    }
    _ = subscriptionDbEp -> close();
}

@Description {value:"Function to initiate set up activities on startup/restart"}
function setupOnStartup() {
    if (hubPersistenceEnabled) {
        if (hubTopicRegistrationRequired) {
            addTopicRegistrationsOnStartup();
        }
        addSubscriptionsOnStartup(); //TODO:verify against topics
    }
    return;
}

@Description {value:"Function to load topic registrations from the database"}
function addTopicRegistrationsOnStartup() {
    endpoint sql:Client subscriptionDbEp {
        url: hubDatabaseUrl,
        username: hubDatabaseUsername,
        password: hubDatabasePassword,
        poolOptions: {maximumPoolSize:5}
    };
    sql:Parameter[] sqlParams = [];
    table dt;
    var dbResult = subscriptionDbEp -> select("SELECT topic, secret FROM topics", TopicRegistration, sqlParams);
    match (dbResult) {
        table t => { dt = t; }
        error sqlErr => {
            log:printError("Error retreiving data from the database: " + sqlErr.message);
        }
    }
    while (dt.hasNext()) {
        match (<TopicRegistration> dt.getNext()) {
            TopicRegistration registrationDetails => {
                string errorMessage = websub:registerTopicAtHub(registrationDetails.topic, registrationDetails.secret,
                                                           loadingOnStartUp = true);
                if (errorMessage != "") {
                    log:printError("Error registering topic details retrieved from the database: " + errorMessage);
                }
            }
            error convError => {
                log:printError("Error retreiving topic registration details from the database: " + convError.message);
            }
        }
    }
    _ = subscriptionDbEp -> close();
}

@Description {value:"Function to add subscriptions to the broker on startup, if persistence is enabled"}
function addSubscriptionsOnStartup() {
    endpoint sql:Client subscriptionDbEp {
        url: hubDatabaseUrl,
        username: hubDatabaseUsername,
        password: hubDatabasePassword,
        poolOptions: {maximumPoolSize:5}
    };

    int time = time:currentTime().time;
    sql:Parameter para1 = (sql:TYPE_BIGINT, time);
    sql:Parameter[] sqlParams = [para1];
    _ = subscriptionDbEp -> update("DELETE FROM subscriptions WHERE ? - lease_seconds > created_at", sqlParams);
    sqlParams = [];
    table dt;
    var dbResult = subscriptionDbEp -> select("SELECT topic, callback, secret, lease_seconds, created_at"
                                                + " FROM subscriptions", websub:SubscriptionDetails, sqlParams);
    match (dbResult) {
        table t => { dt = t; }
        error sqlErr => {
            log:printError("Error retreiving data from the database: " + sqlErr.message);
        }
    }
    while (dt.hasNext()) {
        match (<websub:SubscriptionDetails> dt.getNext()) {
            websub:SubscriptionDetails subscriptionDetails => {
                websub:addSubscription(subscriptionDetails);
            }
            error convError => {
                log:printError("Error retreiving subscription details from the database: " + convError.message);
            }
        }
    }
    _ = subscriptionDbEp -> close();
}

@Description {value:"Function to fetch updates for a particular topic."}
@Param {value:"topic: The topic URL to be fetched to retrieve updates"}
function fetchTopicUpdate(string topic) returns (http:Response | http:HttpConnectorError) {
    endpoint http:Client topicEp {
        targets:[{url:topic, secureSocket: secureSocket }]
    };

    http:Request request = new;

    var fetchResponse = topicEp -> get("", request);
    return fetchResponse;
}

@Description {value:"Function to distribute content to a subscriber on notification from publishers."}
@Param {value:"callback: The callback URL registered for the subscriber"}
@Param {value:"subscriptionDetails: The subscription details for the particular subscriber"}
@Param {value:"payload: The update payload to be delivered to the subscribers"}
public function distributeContent(string callback, websub:SubscriptionDetails subscriptionDetails, json payload) {
    endpoint http:Client callbackEp {
        targets:[{url:callback, secureSocket: secureSocket }]
    };

    http:Request request = new;
    int currentTime = time:currentTime().time;
    int createdAt = subscriptionDetails.createdAt;
    int leaseSeconds = subscriptionDetails.leaseSeconds;

    if (currentTime - leaseSeconds > createdAt) {
        //TODO: introduce a separate periodic task, and modify select to select only active subs
        websub:removeSubscription(subscriptionDetails.topic, callback);
        if (hubPersistenceEnabled) {
            changeSubscriptionInDatabase(websub:MODE_UNSUBSCRIBE, subscriptionDetails);
        }
    } else {
        string stringPayload = payload.toString() but { () => "" };
        request.setHeader(websub:CONTENT_TYPE, mime:APPLICATION_JSON);
        if (subscriptionDetails.secret != "") {
            string xHubSignature = hubSignatureMethod + "=";
            string generatedSignature = "";
            if (websub:SHA1.equalsIgnoreCase(hubSignatureMethod)) { //not recommended
                generatedSignature = crypto:getHmac(stringPayload, subscriptionDetails.secret, crypto:SHA1);
            } else if (websub:SHA256.equalsIgnoreCase(hubSignatureMethod)) {
                generatedSignature = crypto:getHmac(stringPayload, subscriptionDetails.secret, crypto:SHA256);
            } else if (websub:MD5.equalsIgnoreCase(hubSignatureMethod)) {
                generatedSignature = crypto:getHmac(stringPayload, subscriptionDetails.secret, crypto:MD5);
            }
            xHubSignature = xHubSignature + generatedSignature;
            request.setHeader(websub:X_HUB_SIGNATURE, xHubSignature);
        }

        request.setHeader(websub:X_HUB_UUID, util:uuid());
        request.setHeader(websub:X_HUB_TOPIC, subscriptionDetails.topic);
        request.setHeader("Link", buildWebSubLinkHeader(hubPublicUrl, subscriptionDetails.topic));
        request.setJsonPayload(payload);
        var contentDistributionRequest = callbackEp -> post("", request);
        match (contentDistributionRequest) {
            http:Response response => { return; }
            http:HttpConnectorError err => { log:printError("Error delievering content to: " + callback); }
        }
    }
}

@Description {value:"Struct to represent a topic registration"}
@Field {value:"mode: Whether a pending subscription or unsubscription"}
@Field {value:"topic: The topic for which the subscription or unsubscription is pending"}
@Field {value:"callback: The callback specified for the pending subscription or unsubscription"}
type TopicRegistration {
    string topic,
    string secret,
};

@Description {value:"Object to represent a pending subscription/unsubscription request"}
@Field {value:"mode: Whether a pending subscription or unsubscription"}
@Field {value:"topic: The topic for which the subscription or unsubscription is pending"}
@Field {value:"callback: The callback specified for the pending subscription or unsubscription"}
type PendingSubscriptionChangeRequest object {

    public {
        string mode;
        string topic;
        string callback;
    }

    new (mode, topic, callback) {}

    @Description {value:"Function to check if two pending subscription change requests are equal."}
    @Param {value:"pendingRequest: The pending subscription change request to check against"}
    function equals(PendingSubscriptionChangeRequest pendingRequest) returns (boolean) {
        return pendingRequest.mode == mode && pendingRequest.topic == topic && pendingRequest.callback == callback;
    }

};

public function generateKey(string topic, string callback) returns (string) {
    return topic + "_" + callback;
}

@Description {value:"Function to build the link header for a request"}
@Param {value:"hub: The hub publishing the update"}
@Param {value:"topic: The canonical URL of the topic for which the update occurred"}
@Return{value:"The link header content"}
public function buildWebSubLinkHeader (string hub, string topic) returns (string) {
    string linkHeader = "<" + hub + ">; rel=\"hub\", <" + topic + ">; rel=\"self\"";
    return linkHeader;
}
