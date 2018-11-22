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
import ballerina/h2;
import ballerina/log;
import ballerina/mime;
import ballerina/sql;
import ballerina/system;
import ballerina/time;

map<PendingSubscriptionChangeRequest> pendingRequests = {};

@http:ServiceConfig {
    basePath:BASE_PATH
}
service<http:Service> hubService {

    @http:ResourceConfig {
        methods:["GET"],
        path:HUB_PATH
    }
    status(endpoint client, http:Request request) {
        http:Response response = new;
        response.statusCode = http:ACCEPTED_202;
        response.setTextPayload("Ballerina Hub Service - Up and Running!");
        _ = client->respond(response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:HUB_PATH
    }
    hub(endpoint client, http:Request request) {
        http:Response response = new;
        string topic = "";

        var reqFormParamMap = request.getFormParams();
        map<string> params = reqFormParamMap is map<string> ? reqFormParamMap : {};

        string mode = params[HUB_MODE] ?: "";

        var topicFromParams = params[HUB_TOPIC];
        if topicFromParams is string {
            var decodedValue = http:decode(topicFromParams, "UTF-8");
            topic = decodedValue is string ? decodedValue : topicFromParams;
        }

        if (mode == MODE_SUBSCRIBE || mode == MODE_UNSUBSCRIBE) {
            boolean validSubscriptionChangeRequest = false;
            // TODO: check the non-existing key at this point and return the 400
            var result = params[HUB_CALLBACK];
            string callbackFromParams = params[HUB_CALLBACK] ?: "";
            var decodedCallbackFromParams = http:decode(callbackFromParams, "UTF-8");
            string callback = decodedCallbackFromParams is string ? decodedCallbackFromParams : callbackFromParams;
            var validationStatus = validateSubscriptionChangeRequest(mode, topic, callback);
            if (validationStatus is error) {
                response.statusCode = http:BAD_REQUEST_400;
                string errorMessage = <string> validationStatus.detail().message;
                response.setTextPayload(errorMessage);
            } else {
                validSubscriptionChangeRequest = true;
                response.statusCode = http:ACCEPTED_202;
            }

            var responseError = client->respond(response);
            if (responseError is error) {
                log:printError("Error responding to subscription change request", err = responseError);
            } else {
                if (validSubscriptionChangeRequest) {
                    verifyIntentAndAddSubscription(callback, topic, params);
                }
            }
            done;
        } else if (mode == MODE_REGISTER) {
            if (!hubRemotePublishingEnabled || !hubTopicRegistrationRequired) {
                response.statusCode = http:BAD_REQUEST_400;
                response.setTextPayload("Remote topic registration not allowed/not required at the Hub");
                log:printWarn("Remote topic registration denied at Hub");
                var responseError = client->respond(response);
                if (responseError is error) {
                    log:printError("Error responding on remote topic registration failure", err = responseError);
                }
                done;
            }

            var registerStatus = registerTopicAtHub(topic);
            if (registerStatus is error) {
                string errorMessage = <string> registerStatus.detail().message;
                response.statusCode = http:BAD_REQUEST_400;
                response.setTextPayload(errorMessage);
                log:printWarn("Topic registration unsuccessful at Hub for Topic[" + topic + "]: " + errorMessage);
            } else {
                response.statusCode = http:ACCEPTED_202;
                log:printInfo("Topic registration successful at Hub, for topic[" + topic + "]");
            }
            var responseError = client->respond(response);
            if (responseError is error) {
                log:printError("Error responding remote topic registration status", err = responseError);
            }
        } else if (mode == MODE_UNREGISTER) {
            if (!hubRemotePublishingEnabled || !hubTopicRegistrationRequired) {
                response.statusCode = http:BAD_REQUEST_400;
                response.setTextPayload("Remote unregistration not allowed/not required at the Hub");
                log:printWarn("Remote topic unregistration denied at Hub");
                var responseError = client->respond(response);
                if (responseError is error) {
                    log:printError("Error responding on remote topic unregistration failure", err = responseError);
                }
                done;
            }

            var unregisterStatus = unregisterTopicAtHub(topic);
            if (unregisterStatus is error) {
                string errorMessage = <string> unregisterStatus.detail().message;
                response.statusCode = http:BAD_REQUEST_400;
                response.setTextPayload(errorMessage);
                log:printWarn("Topic unregistration unsuccessful at Hub for Topic[" + topic + "]: " + errorMessage);
            } else {
                response.statusCode = http:ACCEPTED_202;
                log:printInfo("Topic unregistration successful at Hub, for topic[" + topic + "]");
            }
            var responseError = client->respond(response);
            if (responseError is error) {
                log:printError("Error responding remote topic unregistration status", err = responseError);
            }
        } else {
            if (mode != MODE_PUBLISH) {
                params = request.getQueryParams();
                mode = params[HUB_MODE] ?: "";
                string topicValue = params[HUB_TOPIC] ?: "";
                var decodedTopic = http:decode(topicValue, "UTF-8");
                topic = decodedTopic is string ? decodedTopic : topicValue;
            }

            if (mode == MODE_PUBLISH && hubRemotePublishingEnabled) {
                if (!hubTopicRegistrationRequired || isTopicRegistered(topic)) {
                    byte[]|error binaryPayload;
                    string stringPayload;
                    string contentType = "";
                    if (hubRemotePublishMode == PUBLISH_MODE_FETCH) {
                        var fetchResponse = fetchTopicUpdate(topic);
                        if (fetchResponse is http:Response) {
                            binaryPayload = fetchResponse.getBinaryPayload();
                            if (fetchResponse.hasHeader(CONTENT_TYPE)) {
                                contentType = fetchResponse.getHeader(CONTENT_TYPE);
                            }
                            var fetchedPayload = fetchResponse.getPayloadAsString();
                            stringPayload = fetchedPayload is string ? fetchedPayload : "";
                        } else if (fetchResponse is error) {
                            string errorCause = <string> fetchResponse.detail().message;
                            string errorMessage = "Error fetching updates for topic URL [" + topic + "]: "
                                                    + errorCause;
                            log:printError(errorMessage);
                            response.setTextPayload(errorMessage);
                            response.statusCode = http:BAD_REQUEST_400;
                            var responseError = client->respond(response);
                            if (responseError is error) {
                                log:printError("Error responding on update fetch failure", err = responseError);
                            }
                            done;
                        } else {
                            // should never reach here
                            done;
                        }
                    } else {
                        binaryPayload = request.getBinaryPayload();
                        if (request.hasHeader(CONTENT_TYPE)) {
                            contentType = request.getHeader(CONTENT_TYPE);
                        }
                        var result = request.getPayloadAsString();
                        stringPayload = result is string ? result : "";
                    }

                    error? publishStatus = ();
                    if (binaryPayload is byte[]) {
                        WebSubContent notification = { payload:binaryPayload, contentType:contentType };
                        publishStatus = publishToInternalHub(topic, notification);
                    } else if (binaryPayload is error) {
                        string errorCause = <string> binaryPayload.detail().message;
                        string errorMessage = "Error extracting payload: " + errorCause;
                        log:printError(errorMessage);
                        response.statusCode = http:BAD_REQUEST_400;
                        response.setTextPayload(errorMessage);
                        var responseError = client->respond(response);
                        if (responseError is error) {
                            log:printError("Error responding on payload extraction failure for"
                                                    + " publish request", err = responseError);
                        }
                        done;
                    }

                    if (publishStatus is error) {
                        string errorCause = <string> publishStatus.detail().message;
                        string errorMessage = "Update notification failed for Topic [" + topic + "]: " + errorCause;
                        response.setTextPayload(errorMessage);
                        log:printError(errorMessage);
                    } else {
                        log:printInfo("Update notification done for Topic [" + topic + "]");
                        response.statusCode = http:ACCEPTED_202;
                        var responseError = client->respond(response);
                        if (responseError is error) {
                            log:printError("Error responding on update notification for topic[" + topic
                                                    + "]", err = responseError);
                        }
                        done;
                    }
                } else {
                    string errorMessage = "Publish request denied for unregistered topic[" + topic + "]";
                    log:printDebug(errorMessage);
                    response.setTextPayload(errorMessage);
                }
                response.statusCode = http:BAD_REQUEST_400;
                var responseError = client->respond(response);
                if (responseError is error) {
                    log:printError("Error responding to publish request", err = responseError);
                }
            } else {
                response.statusCode = http:BAD_REQUEST_400;
                var responseError = client->respond(response);
                if (responseError is error) {
                    log:printError("Error responding to request", err = responseError);
                }
            }
        }
    }
}

# Function to validate a subscription/unsubscription request, by validating the mode, topic and callback specified.
#
# + mode - Mode specified in the subscription change request parameters
# + topic - Topic specified in the subscription change request parameters
# + callback - Callback specified in the subscription change request parameters
# + return - `error` if validation failed for the subscription request
function validateSubscriptionChangeRequest(string mode, string topic, string callback) returns error? {
    if (topic != "" && callback != "") {
        PendingSubscriptionChangeRequest pendingRequest = new(mode, topic, callback);
        pendingRequests[generateKey(topic, callback)] = pendingRequest;
        if (!callback.hasPrefix("http://") && !callback.hasPrefix("https://")) {
            error err = error(WEBSUB_ERROR_CODE, { message : "Malformed URL specified as callback" });
            return err;
        }
        if (hubTopicRegistrationRequired && !isTopicRegistered(topic)) {
            error err = error(WEBSUB_ERROR_CODE, { message : "Subscription request denied for unregistered topic" });
            return err;
        }
        return;
    }
    map errorDetail = { message : "Topic/Callback cannot be null for subscription/unsubscription request" };
    error err = error(WEBSUB_ERROR_CODE, errorDetail);
    return err;
}

# Function to initiate intent verification for a valid subscription/unsubscription request received.
#
# + callback - The callback URL of the new subscription/unsubscription request
# + topic - The topic specified in the new subscription/unsubscription request
# + params - Parameters specified in the new subscription/unsubscription request
function verifyIntentAndAddSubscription(string callback, string topic, map<string> params) {
    endpoint http:Client callbackEp {
        url:callback,
        secureSocket: hubClientSecureSocket
    };

    string mode = params[HUB_MODE] ?: "";
    string strLeaseSeconds = params[HUB_LEASE_SECONDS] ?: "";
    int leaseSeconds = <int>strLeaseSeconds but {error => 0};

    //measured from the time the verification request was made from the hub to the subscriber from the recommendation
    int createdAt = time:currentTime().time;

    if (!(leaseSeconds > 0)) {
        leaseSeconds = hubLeaseSeconds;
    }
    string challenge = system:uuid();

    http:Request request = new;

    string queryParams = HUB_MODE + "=" + mode
        + "&" + HUB_TOPIC + "=" + topic
        + "&" + HUB_CHALLENGE + "=" + challenge;

    if (mode == MODE_SUBSCRIBE) {
        queryParams = queryParams + "&" + HUB_LEASE_SECONDS + "=" + leaseSeconds;
    }

    var subscriberResponse = callbackEp->get(untaint ("?" + queryParams), message = request);

    if (subscriberResponse is http:Response) {
        var respStringPayload = subscriberResponse.getTextPayload();
        if (respStringPayload is string) {
            if (respStringPayload != challenge) {
                log:printInfo("Intent verification failed for mode: [" + mode + "], for callback URL: ["
                        + callback + "]: Challenge not echoed correctly.");
            } else {
                SubscriptionDetails subscriptionDetails = {topic:topic, callback:callback};
                if (mode == MODE_SUBSCRIBE) {
                    subscriptionDetails.leaseSeconds = leaseSeconds * 1000;
                    subscriptionDetails.createdAt = createdAt;
                    subscriptionDetails.secret = params[HUB_SECRET] but { () => "" };
                    if (!isTopicRegistered(topic)) {
                        var registerStatus = registerTopicAtHub(topic);
                        if (registerStatus is error) {
                            string errCause = <string> registerStatus.detail().message;
                            log:printError("Error registering topic for subscription: " + errCause);
                        }
                    }
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
        } else if (respStringPayload is error) {
            string errCause = <string> respStringPayload.detail().message;
            log:printInfo("Intent verification failed for mode: [" + mode + "], for callback URL: [" + callback
                    + "]: Error retrieving response payload: " + errCause);
        }
    } else if (subscriberResponse is error) {
        string errCause = <string> subscriberResponse.detail().message;
        log:printInfo("Error sending intent verification request for callback URL: [" + callback + "]: " + errCause);
    }
    PendingSubscriptionChangeRequest pendingSubscriptionChangeRequest = new(mode, topic, callback);
    string key = generateKey(topic, callback);
    var retrievedRequest = pendingRequests[key];
    if (retrievedRequest is PendingSubscriptionChangeRequest) {
        if (pendingSubscriptionChangeRequest.equals(retrievedRequest)) {
            _ = pendingRequests.remove(key);
        }
    }
}

# Function to add/remove the details of topics registered, in the database.
#
# + mode - Whether the change is for addition/removal
# + topic - The topic for which registration is changing
function changeTopicRegistrationInDatabase(string mode, string topic) {
    endpoint h2:Client subscriptionDbEp {
        path: hubDatabaseDirectory,
        name: hubDatabaseName,
        username: hubDatabaseUsername,
        password: hubDatabasePassword,
        poolOptions: { maximumPoolSize:5 }
    };

    sql:Parameter para1 = {sqlType:sql:TYPE_VARCHAR, value:topic};
    if (mode == MODE_REGISTER) {
        var rowCount = subscriptionDbEp->update("INSERT INTO topics (topic) VALUES (?)", para1);
        if (rowCount is int) {
            log:printInfo("Successfully updated " + rowCount + " entries for registration");
        } else if (rowCount is error) {
            string errCause = <string> rowCount.detail().message;
            log:printError("Error occurred updating registration data: " + errCause);
        }
    } else {
        var rowCount = subscriptionDbEp->update("DELETE FROM topics WHERE topic=?", para1);
        if (rowCount is int) {
            log:printInfo("Successfully updated " + rowCount + " entries for unregistration");
        } else if (rowCount is error) {
            string errCause = <string> rowCount.detail().message;
            log:printError("Error occurred updating unregistration data: " + errCause);
        }
    }
    subscriptionDbEp.stop();
}

# Function to add/change/remove the subscription details in the database.
#
# + mode - Whether the subscription change is for unsubscription/unsubscription
# + subscriptionDetails - The details of the subscription changing
function changeSubscriptionInDatabase(string mode, SubscriptionDetails subscriptionDetails) {
    endpoint h2:Client subscriptionDbEp {
        path: hubDatabaseDirectory,
        name: hubDatabaseName,
        username: hubDatabaseUsername,
        password: hubDatabasePassword,
        poolOptions: { maximumPoolSize:5 }
    };

    sql:Parameter para1 = {sqlType:sql:TYPE_VARCHAR, value:subscriptionDetails.topic};
    sql:Parameter para2 = {sqlType:sql:TYPE_VARCHAR, value:subscriptionDetails.callback};
    if (mode == MODE_SUBSCRIBE) {
        sql:Parameter para3 = {sqlType:sql:TYPE_VARCHAR, value:subscriptionDetails.secret};
        sql:Parameter para4 = {sqlType:sql:TYPE_BIGINT, value:subscriptionDetails.leaseSeconds};
        sql:Parameter para5 = {sqlType:sql:TYPE_BIGINT, value:subscriptionDetails.createdAt};
        var rowCount = subscriptionDbEp->update("INSERT INTO subscriptions"
                + " (topic,callback,secret,lease_seconds,created_at) VALUES (?,?,?,?,?) ON"
                + " DUPLICATE KEY UPDATE secret=?, lease_seconds=?,created_at=?",
            untaint para1, untaint para2, untaint para3, untaint para4, untaint para5);
        if (rowCount is int) {
            log:printInfo("Successfully updated " + rowCount + " entries for subscription");
        } else if (rowCount is error) {
            string errCause = <string> rowCount.detail().message;
            log:printError("Error occurred updating subscription data: " + errCause);
        }
    } else {
        var rowCount = subscriptionDbEp->update("DELETE FROM subscriptions WHERE topic=? AND callback=?",
            untaint para1, untaint para2);

        if (rowCount is int) {
            log:printInfo("Successfully updated " + rowCount + " entries for unsubscription");
        } else if (rowCount is error) {
            string errCause = <string> rowCount.detail().message;
            log:printError("Error occurred updating unsubscription data: " + errCause);
        }
    }
    subscriptionDbEp.stop();
}

# Function to initiate set up activities on startup/restart.
function setupOnStartup() {
    if (hubPersistenceEnabled) {
        addTopicRegistrationsOnStartup();
        addSubscriptionsOnStartup(); //TODO:verify against topics
    }
    return;
}

# Function to load topic registrations from the database.
function addTopicRegistrationsOnStartup() {
    endpoint h2:Client subscriptionDbEp {
        path: hubDatabaseDirectory,
        name: hubDatabaseName,
        username: hubDatabaseUsername,
        password: hubDatabasePassword,
        poolOptions: { maximumPoolSize:5 }
    };
    var dbResult = subscriptionDbEp->select("SELECT * FROM topics", TopicRegistration);
    if (dbResult is table) {
        table dt = dbResult;
        while (dt.hasNext()) {
            var registrationDetails = <TopicRegistration>dt.getNext();
            if (registrationDetails is TopicRegistration) {
                var registerStatus = registerTopicAtHub(registrationDetails.topic, loadingOnStartUp = true);
                if (registerStatus is error) {
                    string errCause = <string> registerStatus.detail().message;
                    log:printError("Error registering topic details retrieved from the database: "+ errCause);
                }
            } else if (registrationDetails is error) {
                string errCause = <string> registrationDetails.detail().message;
                log:printError("Error retreiving topic registration details from the database: " + errCause);
            }
        }
    } else if (dbResult is error) {
        string errCause = <string> dbResult.detail().message;
        log:printError("Error retreiving data from the database: " + errCause);
    }
    subscriptionDbEp.stop();
}

# Function to add subscriptions to the broker on startup, if persistence is enabled.
function addSubscriptionsOnStartup() {
    endpoint h2:Client subscriptionDbEp {
        path: hubDatabaseDirectory,
        name: hubDatabaseName,
        username: hubDatabaseUsername,
        password: hubDatabasePassword,
        poolOptions: { maximumPoolSize:5 }
    };

    int time = time:currentTime().time;
    sql:Parameter para1 = {sqlType:sql:TYPE_BIGINT, value:time};
    _ = subscriptionDbEp->update("DELETE FROM subscriptions WHERE ? - lease_seconds > created_at", para1);

    var dbResult = subscriptionDbEp->select("SELECT topic, callback, secret, lease_seconds, created_at"
            + " FROM subscriptions", SubscriptionDetails);
    if (dbResult is table) {
        table dt = dbResult;
        while (dt.hasNext()) {
            var subscriptionDetails = <SubscriptionDetails>dt.getNext();
            if (subscriptionDetails is SubscriptionDetails) {
                addSubscription(subscriptionDetails);
            } else if (subscriptionDetails is error) {
                string errCause = <string> subscriptionDetails.detail().message;
                log:printError("Error retreiving subscription details from the database: " + errCause);
            }
        }
    } else if (dbResult is error) {
        string errCause = <string> dbResult.detail().message;
        log:printError("Error retreiving data from the database: " + errCause);
    }
    subscriptionDbEp.stop();
}

# Function to delete topic and subscription details from the database at shutdown, if persistence is enabled.
function clearSubscriptionDataInDb() {
    endpoint h2:Client subscriptionDbEp {
        path: hubDatabaseDirectory,
        name: hubDatabaseName,
        username: hubDatabaseUsername,
        password: hubDatabasePassword,
        poolOptions: { maximumPoolSize:5 }
    };

    var dbResult = subscriptionDbEp->update("DELETE FROM subscriptions");
    if (dbResult is error) {
        string errCause = <string> dbResult.detail().message;
        log:printError("Error deleting subscription data from the database: " + errCause);
    }

    dbResult = subscriptionDbEp->update("DELETE FROM topics");
    if (dbResult is error) {
        string errCause = <string> dbResult.detail().message;
        log:printError("Error deleting topic data from the database: " + errCause);
    }

    subscriptionDbEp.stop();
}

# Function to fetch updates for a particular topic.
#
# + topic - The topic URL to be fetched to retrieve updates
# + return - `http:Response` indicating the response received on fetching the topic URL if successful,
#            `error` if an HTTP error occurred
function fetchTopicUpdate(string topic) returns http:Response|error {
    endpoint http:Client topicEp {
        url:topic,
        secureSocket: hubClientSecureSocket
    };

    http:Request request = new;

    var fetchResponse = topicEp->get("", message = request);
    return fetchResponse;
}

# Function to distribute content to a subscriber on notification from publishers.
#
# + callback - The callback URL registered for the subscriber
# + subscriptionDetails - The subscription details for the particular subscriber
# + webSubContent - The content to be sent to subscribers
# + return - Nil if successful, error in case of invalid content-type
function distributeContent(string callback, SubscriptionDetails subscriptionDetails, WebSubContent webSubContent)
returns error? {
    endpoint http:Client callbackEp {
        url:callback,
        secureSocket: hubClientSecureSocket
    };

    http:Request request = new;
    request.setPayload(webSubContent.payload);
    check request.setContentType(webSubContent.contentType);

    int currentTime = time:currentTime().time;
    int createdAt = subscriptionDetails.createdAt;
    int leaseSeconds = subscriptionDetails.leaseSeconds;

    if (currentTime - leaseSeconds > createdAt) {
        //TODO: introduce a separate periodic task, and modify select to select only active subs
        removeSubscription(subscriptionDetails.topic, callback);
        if (hubPersistenceEnabled) {
            changeSubscriptionInDatabase(MODE_UNSUBSCRIBE, subscriptionDetails);
        }
    } else {
        string stringPayload = request.getPayloadAsString() but { error => "" };
        if (subscriptionDetails.secret != "") {
            string xHubSignature = hubSignatureMethod + "=";
            string generatedSignature = "";
            if (SHA1.equalsIgnoreCase(hubSignatureMethod)) { //not recommended
                generatedSignature = crypto:hmac(stringPayload, subscriptionDetails.secret, crypto:SHA1);
            } else if (SHA256.equalsIgnoreCase(hubSignatureMethod)) {
                generatedSignature = crypto:hmac(stringPayload, subscriptionDetails.secret, crypto:SHA256);
            }
            xHubSignature = xHubSignature + generatedSignature;
            request.setHeader(X_HUB_SIGNATURE, xHubSignature);
        }

        request.setHeader(X_HUB_UUID, system:uuid());
        request.setHeader(X_HUB_TOPIC, subscriptionDetails.topic);
        request.setHeader("Link", buildWebSubLinkHeader(hubPublicUrl, subscriptionDetails.topic));
        var contentDistributionResponse = callbackEp->post("", request);
        if (contentDistributionResponse is http:Response) {
            int respStatusCode = contentDistributionResponse.statusCode;
            if (isSuccessStatusCode(respStatusCode)) {
                log:printDebug("Content delivery to callback[" + callback
                                + "] successful for topic[" + subscriptionDetails.topic + "]");
            } else if (respStatusCode == http:GONE_410) {
                removeSubscription(subscriptionDetails.topic, callback);
                if (hubPersistenceEnabled) {
                    changeSubscriptionInDatabase(MODE_UNSUBSCRIBE, subscriptionDetails);
                }
                log:printInfo("HTTP 410 response code received: Subscription deleted for callback[" + callback
                                + "], topic[" + subscriptionDetails.topic + "]");
            } else {
                log:printError("Error delievering content to callback[" + callback + "] for topic["
                            + subscriptionDetails.topic + "]: received response code " + respStatusCode);
            }
        } else if (contentDistributionResponse is error) {
            string errCause = <string> contentDistributionResponse.detail().message;
            log:printError("Error delievering content to callback[" + callback + "] for topic["
                            + subscriptionDetails.topic + "]: " + errCause);
        }
    }
    return;
}

// TODO: validate if no longer necessary
# Struct to represent a topic registration.
#
# + topic - The topic for which notification would happen
type TopicRegistration record {
    string topic = "";
    !...
};

# Object to represent a pending subscription/unsubscription request.
#
# + mode - Whether a pending subscription or unsubscription
# + topic - The topic for which the subscription or unsubscription is pending
# + callback - The callback specified for the pending subscription or unsubscription
type PendingSubscriptionChangeRequest object {

    public string mode;
    public string topic;
    public string callback;

    new (mode, topic, callback) {}

    # Function to check if two pending subscription change requests are equal.
    #
    # + pendingRequest - The pending subscription change request to check against
    #
    # + return - `boolean` indicating whether the requests are equal or not
    function equals(PendingSubscriptionChangeRequest pendingRequest) returns boolean {
        return pendingRequest.mode == self.mode && pendingRequest.topic == self.topic && pendingRequest.callback == self.callback;
    }
};

function generateKey(string topic, string callback) returns (string) {
    return topic + "_" + callback;
}

# Function to build the link header for a request.
#
# + hub - The hub publishing the update
# + topic - The canonical URL of the topic for which the update occurred
# + return - The link header content
function buildWebSubLinkHeader(string hub, string topic) returns (string) {
    string linkHeader = "<" + hub + ">; rel=\"hub\", <" + topic + ">; rel=\"self\"";
    return linkHeader;
}
