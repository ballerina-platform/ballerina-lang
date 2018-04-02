package ballerina.net.websub.hub;

import ballerina/collections;
import ballerina/data.sql;
import ballerina/log;
import ballerina/mime;
import ballerina/net.http;
import ballerina/net.uri;
import ballerina/net.websub;
import ballerina/security.crypto;
import ballerina/time;
import ballerina/util;

endpoint http:ServiceEndpoint hubServiceEP {
    host:hubHost,
    port:hubPort,
    secureSocket:serviceSecureSocket
};

PendingRequests pendingRequests = {};

@http:ServiceConfig {
    basePath:BASE_PATH
}
service<http:Service> hubService bind hubServiceEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:HUB_PATH
    }
    status (endpoint client, http:Request request) {
        http:Response response = { statusCode:202 };
        response.setStringPayload("Ballerina Hub Service - Up and Running!");
        _ = client -> respond(response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:HUB_PATH
    }
    hub (endpoint client, http:Request request) {
        http:Response response = {};

        var reqFormParams = request.getFormParams();
        map params;
        match (reqFormParams) {
            map reqParams => { params = reqParams; }
            mime:EntityError => {
                response = { statusCode:400 };
                _ = client -> respond(response);
                return;
            }
        }

        string mode = <string> params[websub:HUB_MODE];

        if (mode == websub:MODE_SUBSCRIBE || mode == websub:MODE_UNSUBSCRIBE) {
            boolean validSubscriptionRequest = false;
            match (validateSubscriptionChangeRequest(mode, params)) {
                string errorMessage => {
                    response = { statusCode:400 };
                    response.setStringPayload(errorMessage);
                }
                boolean => {
                    validSubscriptionRequest = true;
                    response = { statusCode:202 };
                }
            }
            _ = client -> respond(response);
            if (validSubscriptionRequest) {
                string callback = <string> params[websub:HUB_CALLBACK];
                callback =? uri:decode(callback, "UTF-8");
                verifyIntent(callback, params);
            }
            return;
        } else if (mode == websub:MODE_REGISTER) {
            if (!hubRemotePublishingEnabled || !hubTopicRegistrationRequired) {
                response = { statusCode:400 };
                response.setStringPayload("Remote topic registration not allowed/not required at the Hub");
                log:printWarn("Remote topic registration denied at Hub");
                _ = client -> respond(response);
                return;
            }

            string topic = <string> params[websub:HUB_TOPIC];
            string secret = "";
            if (params.hasKey(websub:PUBLISHER_SECRET)) {
                secret = <string> params[websub:PUBLISHER_SECRET];
            }
            string errorMessage = websub:registerTopic(topic, secret);
            if (errorMessage != "") {
                response = { statusCode:400 };
                response.setStringPayload(errorMessage);
                log:printWarn("Topic registration unsuccessful at Hub for Topic[" + topic + "]: " + errorMessage);
            } else {
                response = { statusCode:202 };
                log:printInfo("Topic registration successful at Hub, for topic[" + topic + "]");
            }
            _ = client -> respond(response);
        } else if (mode == websub:MODE_UNREGISTER) {
            if (!hubRemotePublishingEnabled || !hubTopicRegistrationRequired) {
                response = { statusCode:400 };
                response.setStringPayload("Remote unregistration not allowed/not required at the Hub");
                log:printWarn("Remote topic unregistration denied at Hub");
                _ = client -> respond(response);
                return;
            }

            string topic = <string> params[websub:HUB_TOPIC];
            string secret = "";
            if (params.hasKey(websub:PUBLISHER_SECRET)) {
                secret = <string> params[websub:PUBLISHER_SECRET];
            }
            string errorMessage = websub:unregisterTopic(topic, secret);
            if (errorMessage != "") {
                response = { statusCode:400 };
                response.setStringPayload(errorMessage);
                log:printWarn("Topic unregistration unsuccessful at Hub for Topic[" + topic + "]: " + errorMessage);
            } else {
                response = { statusCode:202 };
                log:printInfo("Topic unregistration successful at Hub, for topic[" + topic + "]");
            }
            _ = client -> respond(response);
        } else {
            params = request.getQueryParams();
            mode = <string> params[websub:HUB_MODE];

            if (mode == websub:MODE_PUBLISH && hubRemotePublishingEnabled) {
                string topic = <string> params[websub:HUB_TOPIC];
                if (!hubTopicRegistrationRequired || websub:isTopicRegistered(topic)) {
                    var reqJsonPayload = request.getJsonPayload(); //TODO: allow others
                    match (reqJsonPayload) {
                        json payload => {
                            response = { statusCode:202 };
                            _ = client -> respond(response);
                            if (hubTopicRegistrationRequired) {
                                string secret = websub:retrievePublisherSecret(topic);
                                if (secret != "") {
                                    if (request.hasHeader(websub:PUBLISHER_SIGNATURE)) {
                                        string publisherSignature = request.getHeader(websub:PUBLISHER_SIGNATURE);
                                        var signatureValidation = websub:validateSignature(publisherSignature,
                                                                                           payload.toString(), secret);
                                        match (signatureValidation) {
                                            websub:WebSubError err => {
                                                log:printWarn("Signature validation failed for publish request for "
                                                              + "topic[" + topic + "]: " + err.errorMessage);
                                                return;
                                            }
                                            null => {
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
                            return;
                        }
                        http:PayloadError payloadError => {
                            log:printError("Error retreiving payload for WebSub publish request: "
                                           + payloadError.message);
                        }
                    }
                }
                response = { statusCode:400 };
                _ = client -> respond(response);
                return;
            } else {
                response = { statusCode:400 };
                _ = client -> respond(response);
            }
        }
    }
}

@Description {value:"Function to validate a subscription/unsubscription request, by validating the mode, topic and
                    callback specified."}
@Param {value:"mode: Mode of the subscription change request parameters"}
@Param {value:"params: Form params specifying subscription request parameters"}
@Return {value:"Whether the subscription/unsubscription request is valid"}
@Return {value:"If invalid, the error with the subscription/unsubscription request"}
function validateSubscriptionChangeRequest(string mode, map params) returns (boolean|string) {
    string topic = <string> params[websub:HUB_TOPIC];
    string callback = <string> params[websub:HUB_CALLBACK];
    callback =? uri:decode(callback, "UTF-8");
    if (topic != "" && callback != "") {
        PendingSubscriptionChangeRequest pendingRequest = { mode:mode, topic:topic, callback:callback};
        pendingRequests.registerPendingRequest(pendingRequest);
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
function verifyIntent(string callback, map params) {
    endpoint http:ClientEndpoint callbackEp {
        targets:[{ uri:callback, secureSocket: secureSocket }]
    };

    string mode = <string> params[websub:HUB_MODE];
    string topic = <string> params[websub:HUB_TOPIC];
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

    http:Request request = {};

    string queryParams = websub:HUB_MODE + "=" + mode
                         + "&" + websub:HUB_TOPIC + "=" + topic
                         + "&" + websub:HUB_CHALLENGE + "=" + challenge
                         + "&" + websub:HUB_LEASE_SECONDS + "=" + leaseSeconds;

    var subscriberResponse = callbackEp -> get("?" + queryParams, request);

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
    PendingSubscriptionChangeRequest  pendingSubscriptionChangeRequest = {topic:topic, callback:callback, mode:mode};
    pendingRequests.removePendingRequest(pendingSubscriptionChangeRequest);
}

@Description {value:"Function to add/remove the details of topics registered, in the database"}
@Param {value:"mode: Whether the change is for addition/removal"}
@Param {value:"topic: The topic for which registration is changing"}
@Param {value:"secret: The secret if specified when registering, empty string if not"}
function changeTopicRegistrationInDatabase(string mode, string topic, string secret) {
    endpoint sql:Client subscriptionDbEp {
        database: sql:DB.MYSQL,
        host: hubDatabaseHost,
        port: hubDatabasePort,
        name: hubDatabaseName,
        username: hubDatabaseUsername,
        password: hubDatabasePassword,
        options: {maximumPoolSize:5}
    };

    sql:Parameter para1 = {sqlType:sql:Type.VARCHAR, value:topic};
    sql:Parameter para2 = {sqlType:sql:Type.VARCHAR, value:secret};
    sql:Parameter[] sqlParams = [para1, para2];
    if (mode == websub:MODE_REGISTER) {
        var updateStatus = subscriptionDbEp -> update("INSERT INTO topics (topic,secret) VALUES (?,?)", sqlParams);
        match (updateStatus) {
            int rowCount => log:printInfo("Successfully updated " + rowCount + " entries for registration");
            sql:SQLConnectorError err => log:printError("Error occurred updating registration data: " + err.message);
        }
    } else {
        var updateStatus = subscriptionDbEp -> update("DELETE FROM topics WHERE topic=? AND secret=?",
                                                      sqlParams);
        match (updateStatus) {
            int rowCount => log:printInfo("Successfully updated " + rowCount + " entries for unregistration");
            sql:SQLConnectorError err => log:printError("Error occurred updating unregistration data: " + err.message);
        }
    }
    _ = subscriptionDbEp -> close();
}

@Description {value:"Function to add/change/remove the subscription details in the database"}
@Param {value:"mode: Whether the subscription change is for unsubscription/unsubscription"}
@Param {value:"subscriptionDetails: The details of the subscription changing"}
function changeSubscriptionInDatabase(string mode, websub:SubscriptionDetails subscriptionDetails) {
    endpoint sql:Client subscriptionDbEp {
        database: sql:DB.MYSQL,
        host: hubDatabaseHost,
        port: hubDatabasePort,
        name: hubDatabaseName,
        username: hubDatabaseUsername,
        password: hubDatabasePassword,
        options: {maximumPoolSize:5}
    };

    sql:Parameter para1 = {sqlType:sql:Type.VARCHAR, value:subscriptionDetails.topic};
    sql:Parameter[] sqlParams;
    if (mode == websub:MODE_SUBSCRIBE) {
        sql:Parameter para2 = {sqlType:sql:Type.VARCHAR, value:subscriptionDetails.callback};
        sql:Parameter para3 = {sqlType:sql:Type.VARCHAR, value:subscriptionDetails.secret};
        sql:Parameter para4 = {sqlType:sql:Type.BIGINT, value:subscriptionDetails.leaseSeconds};
        sql:Parameter para5 = {sqlType:sql:Type.BIGINT, value:subscriptionDetails.createdAt};
        sqlParams = [para1, para2, para3, para4, para5, para3, para4, para5];
        var updateStatus = subscriptionDbEp -> update("INSERT INTO subscriptions"
                                             + " (topic,callback,secret,lease_seconds,created_at) VALUES (?,?,?,?,?) ON"
                                             + " DUPLICATE KEY UPDATE secret=?, lease_seconds=?,created_at=?",
                                             sqlParams);
        match (updateStatus) {
            int rowCount => log:printInfo("Successfully updated " + rowCount + " entries for subscription");
            sql:SQLConnectorError err => log:printError("Error occurred updating subscription data: " + err.message);
        }
    } else {
        string unsubscribingTopic = subscriptionDetails.callback;
        if (!unsubscribingTopic.hasSuffix("/")) {
            unsubscribingTopic = unsubscribingTopic + "/";
        }
        sql:Parameter para2 = {sqlType:sql:Type.VARCHAR, value:unsubscribingTopic};
        sqlParams = [para1, para2];
        var updateStatus = subscriptionDbEp -> update(
                                               "DELETE FROM subscriptions WHERE topic=? AND callback=?", sqlParams);
        match (updateStatus) {
            int rowCount => log:printInfo("Successfully updated " + rowCount + " entries for unsubscription");
            sql:SQLConnectorError err => log:printError("Error occurred updating unsubscription data: " + err.message);
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
        database: sql:DB.MYSQL,
        host: hubDatabaseHost,
        port: hubDatabasePort,
        name: hubDatabaseName,
        username: hubDatabaseUsername,
        password: hubDatabasePassword,
        options: {maximumPoolSize:5}
    };
    sql:Parameter[] sqlParams = [];
    table dt = {};
    var dbResult = subscriptionDbEp -> select("SELECT topic, secret FROM topics", sqlParams, typeof TopicRegistration);
    match (dbResult) {
        table t => { dt = t; }
        sql:SQLConnectorError sqlErr => {
            log:printError("Error retreiving data from the database: " + sqlErr.message);
        }
    }
    while (dt.hasNext()) {
        match (<TopicRegistration> dt.getNext()) {
            TopicRegistration registrationDetails => {
                string errorMessage = websub:registerTopic(registrationDetails.topic, registrationDetails.secret,
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
        database: sql:DB.MYSQL,
        host: hubDatabaseHost,
        port: hubDatabasePort,
        name: hubDatabaseName,
        username: hubDatabaseUsername,
        password: hubDatabasePassword,
        options: {maximumPoolSize:5}
    };

    int time = time:currentTime().time;
    sql:Parameter para1 = {sqlType:sql:Type.BIGINT, value:time};
    sql:Parameter[] sqlParams = [para1];
    _ = subscriptionDbEp -> update("DELETE FROM subscriptions WHERE ? - lease_seconds > created_at", sqlParams);
    sqlParams = [];
    table dt = {};
    var dbResult = subscriptionDbEp -> select("SELECT topic, callback, secret, lease_seconds, created_at"
                                                + " FROM subscriptions", sqlParams, typeof websub:SubscriptionDetails);
    match (dbResult) {
        table t => { dt = t; }
        sql:SQLConnectorError sqlErr => {
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

@Description {value:"Function to distribute content to a subscriber on notification from publishers."}
@Param {value:"callback: The callback URL registered for the subscriber"}
@Param {value:"subscriptionDetails: The subscription details for the particular subscriber"}
@Param {value:"payload: The update payload to be delivered to the subscribers"}
public function distributeContent(string callback, websub:SubscriptionDetails subscriptionDetails, json payload) {
    endpoint http:ClientEndpoint callbackEp {
        targets:[{ uri:callback, secureSocket: secureSocket }]
    };

    http:Request request = {};
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
        string stringPayload = payload.toString();
        request.setJsonPayload(payload);
        request.setHeader(websub:CONTENT_TYPE, mime:APPLICATION_JSON);
        if (subscriptionDetails.secret != "") {
            string xHubSignature = hubSignatureMethod + "=";
            string generatedSignature = "";
            if (websub:SHA1.equalsIgnoreCase(hubSignatureMethod)) { //not recommended
                generatedSignature = crypto:getHmac(stringPayload, subscriptionDetails.secret, crypto:Algorithm.SHA1);
            } else if (websub:SHA256.equalsIgnoreCase(hubSignatureMethod)) {
                generatedSignature = crypto:getHmac(stringPayload, subscriptionDetails.secret, crypto:Algorithm.SHA256);
            } else if (websub:MD5.equalsIgnoreCase(hubSignatureMethod)) {
                generatedSignature = crypto:getHmac(stringPayload, subscriptionDetails.secret, crypto:Algorithm.MD5);
            }
            xHubSignature = xHubSignature + generatedSignature;
            request.setHeader(websub:X_HUB_SIGNATURE, xHubSignature);
        }

        request.setHeader(websub:X_HUB_UUID, util:uuid());
        request.setHeader(websub:X_HUB_TOPIC, subscriptionDetails.topic);
        request.setHeader("Link", buildWebSubLinkHeader(getHubUrl(), subscriptionDetails.topic));
        var contentDistributionRequest = callbackEp -> post("/", request);
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
struct TopicRegistration {
    string topic;
    string secret;
}

@Description {value:"Struct to represent a pending subscription/unsubscription request"}
@Field {value:"mode: Whether a pending subscription or unsubscription"}
@Field {value:"topic: The topic for which the subscription or unsubscription is pending"}
@Field {value:"callback: The callback specified for the pending subscription or unsubscription"}
struct PendingSubscriptionChangeRequest {
    string mode;
    string topic;
    string callback;
}

@Description {value:"Struct to represent pending subscription/unsubscription requests"}
@Field {value:"pendingRequestVector: Vector containing pending requests"}
struct PendingRequests {
    collections:Vector pendingRequestVector;
}

@Description {value:"Function to register a pending subscription/unsubscription request."}
@Param {value:"pendingSubscriptionChangeRequest: The pending subscription/unsubscription request to add"}
function <PendingRequests pendingRequests> registerPendingRequest
(PendingSubscriptionChangeRequest pendingSubscriptionChangeRequest) {
    if (pendingRequests.pendingRequestVector == null) {
        pendingRequests.pendingRequestVector = {vec:[]};
    }
    pendingRequests.pendingRequestVector.add(pendingSubscriptionChangeRequest);
}

@Description {value:"Function to remove a pending subscription/unsubscription request, on intent verfication."}
@Param {value:"pendingSubscriptionChangeRequest: The pending subscription/unsubscription request to remove"}
function <PendingRequests pendingRequests> removePendingRequest
(PendingSubscriptionChangeRequest pendingSubscriptionChangeRequest) {
    int index = 0;
    collections:Vector vector = pendingRequests.pendingRequestVector;
    int vectorSize = vector.size();
    int removeIndex = vectorSize;
    while (index < vectorSize) {
        match (<PendingSubscriptionChangeRequest> vector.get(index)) {
            PendingSubscriptionChangeRequest tempSubscriptionChangeRequest => {
                if (pendingSubscriptionChangeRequest.equals(tempSubscriptionChangeRequest)) {
                    removeIndex = index;
                    break;
                }
                index = index + 1;
            }
            error convError => {
                log:printError("Error removing pending subscription details: " + convError.message);
            }
        }
    }
    if (removeIndex < vectorSize) {
        _ = vector.remove(removeIndex);
    }
}

@Description {value:"Function to check if two pending subscription change requests are equal."}
@Param {value:"pendingRequestTwo: The pending subscription change request to check against"}
function <PendingSubscriptionChangeRequest pendingRequestOne> equals
(PendingSubscriptionChangeRequest pendingRequestTwo) returns (boolean) {
    return pendingRequestOne.topic == pendingRequestTwo.topic
           && pendingRequestOne.callback == pendingRequestTwo.callback
           && pendingRequestOne.mode == pendingRequestTwo.mode;
}

@Description {value:"Function to build the link header for a request"}
@Param {value:"hub: The hub publishing the update"}
@Param {value:"topic: The canonical URL of the topic for which the update occurred"}
@Return{value:"The link header content"}
public function buildWebSubLinkHeader (string hub, string topic) returns (string) {
    string linkHeader = "<" + hub + ">; rel=\"hub\", <" + topic + ">; rel=\"self\"";
    return linkHeader;
}
