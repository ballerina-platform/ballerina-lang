package ballerina.net.websub.hub;

import ballerina/collections;
import ballerina/data.sql;
import ballerina/log;
import ballerina/mime;
import ballerina/net.http;
import ballerina/net.websub;
import ballerina/security.crypto;
import ballerina/time;
import ballerina/util;

endpoint http:ServiceEndpoint hubServiceEP {
    host:hubHost,
    port:hubPort
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
        response.setStringPayload("Up and Running!");
        _ = client -> respond(response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:HUB_PATH
    }
    hub (endpoint client, http:Request request) {
        http:Response response;
        map params;
        mime:EntityError entityError;
        params, entityError = request.getFormParams();
        if (entityError == null) {
            var mode, _ = (string) params[websub:HUB_MODE];
            var topic, _ = (string) params[websub:HUB_TOPIC];
            boolean validSubscriptionRequest = false;
            if (mode != null) {
                string errorMessage;
                validSubscriptionRequest, errorMessage = validateSubscriptionChangeRequest(params);
                if (!validSubscriptionRequest) {
                    response = { statusCode:400 };
                    response.setStringPayload(errorMessage);
                } else {
                    response = { statusCode:202 };
                }
                _ = client -> respond(response);
            } else {
                params = request.getQueryParams();
                mode, _ = (string) params[websub:HUB_MODE];
                if (mode == websub:MODE_PUBLISH) {
                    topic, _ = (string) params[websub:HUB_TOPIC];
                    json payload;
                    payload, entityError = request.getJsonPayload(); //TODO: allow others
                    if (entityError != null) {
                        response = { statusCode:400 };
                        _ = client -> respond(response);
                    } else {
                        response = { statusCode:202 };
                        _ = client -> respond(response);
                        publishToInternalHub(topic, payload);
                        log:printInfo("Event notification done for Topic [" + topic + "]");
                    }
                } else {
                    response = { statusCode:400 };
                    _ = client -> respond(response);
                }
            }

            if (validSubscriptionRequest) {
                var callback, _ = (string) params[websub:HUB_CALLBACK];
                verifyIntent(callback, params);
            }
        } else {
            response = { statusCode:400 };
            _ = client -> respond(response);
        }
    }
}

@Description {value:"Function to validate a subscription/unsubscription request, by validating the mode, topic and
                    callback specified."}
@Param {value:"params: Form params specifying subscription request parameters"}
@Return {value:"Whether the subscription/unsubscription request is valid"}
@Return {value:"If invalid, the error with the subscription/unsubscription request"}
function validateSubscriptionChangeRequest(map params) (boolean, string) {
    var mode, _ = (string) params[websub:HUB_MODE];
    var topic, _ = (string) params[websub:HUB_TOPIC];
    var callback, _ = (string) params[websub:HUB_CALLBACK];

    boolean valid = false;
    string errorMessage;
    if (topic != null && callback != null) {
        PendingSubscriptionChangeRequest pendingRequest = {topic:topic, callback:callback};
        if (mode == websub:MODE_SUBSCRIBE || mode == websub:MODE_UNSUBSCRIBE) {
            pendingRequest.mode = mode;
            pendingRequests.registerPendingRequest(pendingRequest);
            //TODO: Check if topic is valid, further validation of callback (check if a valid URL)
            valid = true;
        } else {
            errorMessage = "Invalide mode. [subscribe|unsubscribe] required.";
        }
    } else {
        errorMessage = "Topic/Callback cannot be null for subscription/unsubscription request";
    }
    return valid, errorMessage;
}

@Description {value:"Function to initiate intent verification for a valid subscription/unsubscription request received."}
@Param {value:"callback: The callback URL of the new subscription/unsubscription request"}
@Param {value:"params: Parameters specified in the new subscription/unsubscription request"}
function verifyIntent(string callback, map params) {
    endpoint http:ClientEndpoint callbackEp {
        targets:[{ uri:callback }]
    };

    var mode, _ = (string) params[websub:HUB_MODE];
    var topic, _ = (string) params[websub:HUB_TOPIC];
    var secret, _ = (string) params[websub:HUB_SECRET];
    var leaseSeconds, _ = (int) params[websub:HUB_LEASE_SECONDS];

    //measured from the time the verification request was made from the hub to the subscriber from the recommendation
    int createdAt = time:currentTime().time;

    if (!(leaseSeconds > 0)) {
          leaseSeconds = hubLeaseSeconds;
    }
    string challenge = util:uuid();

    http:Request request = {};
    http:Response response = {};
    http:HttpConnectorError err;
    string queryParams = websub:HUB_MODE + "=" + mode
                         + "&" + websub:HUB_TOPIC + "=" + topic
                         + "&" + websub:HUB_CHALLENGE + "=" + challenge
                         + "&" + websub:HUB_LEASE_SECONDS + "=" + leaseSeconds;

    response, err = callbackEp -> get("?" + queryParams, request);

    string payload;
    mime:EntityError entityError;
    if (err == null) {
        payload, entityError = response.getStringPayload();
    }
    if (entityError != null) {
        log:printInfo("Intent verification failed for mode: [" + mode + "], for callback URL: [" + callback
            + "]: Error retrieving response payload: " + entityError.message);
    } else if (payload != challenge) {
        log:printInfo("Intent verification failed for mode: [" + mode + "], for callback URL: [" + callback
            + "]: Challenge not echoed correctly.");
    } else {
        SubscriptionDetails subscriptionDetails = {topic:topic, callback:callback, secret:secret,
                                              leaseSeconds:leaseSeconds, createdAt:createdAt};
        if (mode == websub:MODE_SUBSCRIBE) {
            addSubscription(subscriptionDetails);
        } else {
            removeSubscription(topic, callback);
        }

        if (hubPersistenceEnabled) {
            changeSubscriptionInDatabase(mode, subscriptionDetails);
        }
        log:printInfo("Intent verification successful for mode: [" + mode + "], for callback URL: ["
            + callback + "]");
    }
    PendingSubscriptionChangeRequest  pendingSubscriptionChangeRequest = {topic:topic, callback:callback, mode:mode};
    pendingRequests.removePendingRequest(pendingSubscriptionChangeRequest);
}

@Description {value:"Function to add/change/remove the subscription details in the database"}
@Param {value:"mode: Whether the subscription change is for unsubscription/unsubscription"}
@Param {value:"subscriptionDetails: The details of the subscription changing"}
function changeSubscriptionInDatabase(string mode, SubscriptionDetails subscriptionDetails) {
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
    sql:Parameter para2 = {sqlType:sql:Type.VARCHAR, value:subscriptionDetails.callback};
    sql:Parameter[] sqlParams;
    if (mode == websub:MODE_SUBSCRIBE) {
        sql:Parameter para3 = {sqlType:sql:Type.VARCHAR, value:subscriptionDetails.secret};
        sql:Parameter para4 = {sqlType:sql:Type.BIGINT, value:subscriptionDetails.leaseSeconds};
        sql:Parameter para5 = {sqlType:sql:Type.BIGINT, value:subscriptionDetails.createdAt};
        sqlParams = [para1, para2, para3, para4, para5, para3, para4, para5];
        _ = subscriptionDbEp -> update("INSERT INTO subscriptions"
                                             + " (topic,callback,secret,lease_seconds,created_at) VALUES (?,?,?,?,?) ON"
                                             + "DUPLICATE KEY UPDATE secret=?, lease_seconds=?,created_at=?",
                                             sqlParams);
    } else {
        sqlParams = [para1, para2];
        _ = subscriptionDbEp -> update("DELETE FROM subscriptions WHERE topic=? AND callback=?", sqlParams);
    }
    subscriptionDbEp -> close();
}

@Description {value:"Function to initiate set up activities on startup/restart"}
function setupOnStartup() {
    if (hubPersistenceEnabled) {
        _ = addSubscriptionsOnStartup;
    }
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
    table dt = subscriptionDbEp -> select("SELECT topic, callback, secret, lease_seconds, created_at"
                                                + " FROM subscriptions", sqlParams, typeof SubscriptionDetails);
    while (dt.hasNext()) {
        var subscription, _ = (SubscriptionDetails) dt.getNext();
        SubscriptionDetails subscriptionDetails = { topic:subscription.topic, callback:subscription.callback,
                                                      secret:subscription.secret,
                                                      leaseSeconds:subscription.leaseSeconds,
                                                      createdAt:subscription.createdAt};
        addSubscription(subscriptionDetails);
    }
    subscriptionDbEp -> close();
}

@Description {value:"Function to distribute content to a subscriber on notification from publishers."}
@Param {value:"callback: The callback URL registered for the subscriber"}
@Param {value:"subscriptionDetails: The subscription details for the particular subscriber"}
@Param {value:"payload: The update payload to be delivered to the subscribers"}
public function distributeContent(string callback, SubscriptionDetails subscriptionDetails, json payload) {
    endpoint http:ClientEndpoint callbackEp {
        targets:[{ uri:callback }]
    };
                   
    http:Request request = {};
    http:Response response = {};
    http:HttpConnectorError err = {};
    int currentTime = time:currentTime().time;
    int createdAt = subscriptionDetails.createdAt;
    int leaseSeconds = subscriptionDetails.leaseSeconds;

    if (currentTime - leaseSeconds > createdAt) {
        //TODO: introduce a separate periodic task, and modify select to select only active subs
        removeSubscription(subscriptionDetails.topic, callback);
        if (hubPersistenceEnabled) {
            changeSubscriptionInDatabase(websub:MODE_UNSUBSCRIBE, subscriptionDetails);
        }
    } else {
        string stringPayload = payload.toString();
        request.setJsonPayload(payload);
        request.setHeader(websub:CONTENT_TYPE, mime:APPLICATION_JSON);

        if (subscriptionDetails.secret != "null") {
            string xHubSignature = hubSignatureMethod + "=";
            string generatedSignature = null;
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
        response, err = callbackEp -> post("/", request);
        if (err != null) {
            log:printError("Error delievering content to: " + callback);
        }
    }
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
    PendingSubscriptionChangeRequest  tempSubscriptionChangeRequest;
    while (index < vectorSize) {
        tempSubscriptionChangeRequest, _ = (PendingSubscriptionChangeRequest) vector.get(index);
        if (pendingSubscriptionChangeRequest.equals(tempSubscriptionChangeRequest)) {
            removeIndex = index;
            break;
        }
        index = index + 1;
    }
    if (removeIndex < vectorSize) {
        tempSubscriptionChangeRequest, _ = (PendingSubscriptionChangeRequest) vector.remove(removeIndex);
    }
}

@Description {value:"Function to check if two pending subscription change requests are equal."}
@Param {value:"pendingRequestTwo: The pending subscription change request to check against"}
function <PendingSubscriptionChangeRequest pendingRequestOne> equals
(PendingSubscriptionChangeRequest pendingRequestTwo) (boolean) {
    return pendingRequestOne.topic == pendingRequestTwo.topic
           && pendingRequestOne.callback == pendingRequestTwo.callback
           && pendingRequestOne.mode == pendingRequestTwo.mode;
}

@Description {value:"Adds a new subscription for the specified topic in the Ballerina Hub"}
@Param {value:"subscriptionDetails: The details of the subscription including WebSub specifics"}
native function addSubscription (SubscriptionDetails subscriptionDetails);

@Description {value:"Publishes an update against the topic in the Ballerina Hub"}
@Param {value:"topic: The topic for which the update should happen"}
@Param {value:"payload: The update payload"}
native function publishToInternalHub (string topic, json payload);

@Description {value:"Removes a subscription added for the specified topic in the Ballerina Hub"}
@Param {value:"topic: The topic for which the subscription was added"}
@Param {value:"callback: The callback registered for this subscription"}
native function removeSubscription (string topic, string callback);

@Description {value:"Struct to represent Subscription Details retrieved from the database"}
@Field {value:"topic: The topic for which the subscription is added"}
@Field {value:"callback: The callback specified for the particular subscription"}
@Field {value:"secret: The secret to be used for authenticated content distribution"}
@Field {value:"leaseSeconds: The lease second period specified for the particular subscription"}
@Field {value:"createdAt: The time at which the subscription was created"}
struct SubscriptionDetails {
    string topic;
    string callback;
    string secret;
    int leaseSeconds;
    int createdAt;
}
