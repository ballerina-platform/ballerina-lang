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

import ballerina/cache;
import ballerina/crypto;
import ballerina/encoding;
import ballerina/http;
import ballerina/lang.'int as langint;
import ballerina/log;
import ballerina/stringutils;
import ballerina/system;
import ballerina/time;

@tainted map<PendingSubscriptionChangeRequest> pendingRequests = {};

# This cache is used for caching HTTP clients against the subscriber callbacks.
cache:Cache subscriberCallbackClientCache = new (expiryTimeInMillis = DEFAULT_CACHE_EXPIRY_MILLIS);

function getHubService() returns service {
    return @http:ServiceConfig {
        basePath: hubBasePath,
        auth: hubServiceAuth
    }
    service {

        @http:ResourceConfig {
            methods: ["POST"],
            path: hubPublishResourcePath,
            auth: hubPublisherResourceAuth
        }
        resource function publish(http:Caller httpCaller, http:Request request) {
            http:Response response = new;
            string topic = "";

            var reqFormParamMap = request.getFormParams();
            map<string> params = reqFormParamMap is map<string> ? reqFormParamMap : {};

            string mode = params[HUB_MODE] ?: "";

            var topicFromParams = params[HUB_TOPIC];
            if topicFromParams is string {
                var decodedValue = encoding:decodeUriComponent(topicFromParams, "UTF-8");
                topic = decodedValue is string ? decodedValue : topicFromParams;
            }

            if (mode == MODE_REGISTER) {
                if (!remotePublishConfig.enabled || !hubTopicRegistrationRequired) {
                    response.statusCode = http:STATUS_BAD_REQUEST;
                    response.setTextPayload("Remote topic registration not allowed/not required at the Hub");
                    log:printWarn("Remote topic registration denied at Hub");
                    var responseError = httpCaller->respond(response);
                    if (responseError is error) {
                        log:printError("Error responding on remote topic registration failure", responseError);
                    }
                    return;
                }

                var registerStatus = registerTopic(topic);
                if (registerStatus is error) {
                    string errorMessage = <string>registerStatus.detail()?.message;
                    response.statusCode = http:STATUS_BAD_REQUEST;
                    response.setTextPayload(errorMessage);
                    log:printWarn("Topic registration unsuccessful at Hub for Topic[" + topic + "]: " + errorMessage);
                } else {
                    response.statusCode = http:STATUS_ACCEPTED;
                    log:printInfo("Topic registration successful at Hub, for topic[" + topic + "]");
                }
                var responseError = httpCaller->respond(response);
                if (responseError is error) {
                    log:printError("Error responding remote topic registration status", responseError);
                }
            } else if (mode == MODE_UNREGISTER) {
                if (!remotePublishConfig.enabled || !hubTopicRegistrationRequired) {
                    response.statusCode = http:STATUS_BAD_REQUEST;
                    response.setTextPayload("Remote unregistration not allowed/not required at the Hub");
                    log:printWarn("Remote topic unregistration denied at Hub");
                    var responseError = httpCaller->respond(response);
                    if (responseError is error) {
                        log:printError("Error responding on remote topic unregistration failure", responseError);
                    }
                    return;
                }

                var unregisterStatus = unregisterTopic(topic);
                if (unregisterStatus is error) {
                    string errorMessage = <string>unregisterStatus.detail()?.message;
                    response.statusCode = http:STATUS_BAD_REQUEST;
                    response.setTextPayload(errorMessage);
                    log:printWarn("Topic unregistration unsuccessful at Hub for Topic[" + topic + "]: " + errorMessage);
                } else {
                    response.statusCode = http:STATUS_ACCEPTED;
                    log:printInfo("Topic unregistration successful at Hub, for topic[" + topic + "]");
                }
                var responseError = httpCaller->respond(response);
                if (responseError is error) {
                    log:printError("Error responding remote topic unregistration status", responseError);
                }
            } else {
                if (mode != MODE_PUBLISH) {
                    mode = request.getQueryParamValue(HUB_MODE) ?: "";
                    string topicValue = request.getQueryParamValue(HUB_TOPIC) ?: "";
                    var decodedTopic = encoding:decodeUriComponent(topicValue, "UTF-8");
                    topic = decodedTopic is string ? decodedTopic : topicValue;
                }

                if (mode == MODE_PUBLISH && remotePublishConfig.enabled) {
                    if (!hubTopicRegistrationRequired || isTopicRegistered(topic)) {
                        byte[0] arr = [];
                        byte[] | error binaryPayload = arr;
                        string stringPayload;
                        string contentType = "";
                        if (remotePublishConfig.mode == PUBLISH_MODE_FETCH) {
                            var fetchResponse = fetchTopicUpdate(topic);
                            if (fetchResponse is http:Response) {
                                binaryPayload = fetchResponse.getBinaryPayload();
                                if (fetchResponse.hasHeader(CONTENT_TYPE)) {
                                    contentType = fetchResponse.getHeader(CONTENT_TYPE);
                                }
                                var fetchedPayload = fetchResponse.getTextPayload();
                                stringPayload = fetchedPayload is string ? fetchedPayload : "";
                            } else {
                                string errorCause = <string>fetchResponse.detail()?.message;
                                string errorMessage = "Error fetching updates for topic URL [" + topic + "]: "
                                + errorCause;
                                log:printError(errorMessage);
                                response.setTextPayload(<@untaintedstring>errorMessage);
                                response.statusCode = http:STATUS_BAD_REQUEST;
                                var responseError = httpCaller->respond(response);
                                if (responseError is error) {
                                    log:printError("Error responding on update fetch failure", responseError);
                                }
                                return;
                            }
                        } else {
                            binaryPayload = request.getBinaryPayload();
                            if (request.hasHeader(CONTENT_TYPE)) {
                                contentType = request.getHeader(CONTENT_TYPE);
                            }
                            var result = request.getTextPayload();
                            stringPayload = result is string ? result : "";
                        }

                        error? publishStatus = ();
                        if (binaryPayload is byte[]) {
                            WebSubContent notification = {payload: binaryPayload, contentType: contentType};
                            publishStatus = publishToInternalHub(topic, notification);
                        } else {
                            string errorCause = <string>binaryPayload.detail()?.message;
                            string errorMessage = "Error extracting payload: " + <@untaintedstring>errorCause;
                            log:printError(errorMessage);
                            response.statusCode = http:STATUS_BAD_REQUEST;
                            response.setTextPayload(errorMessage);
                            var responseError = httpCaller->respond(response);
                            if (responseError is error) {
                                log:printError("Error responding on payload extraction failure for"
                                + " publish request", responseError);
                            }
                            return;
                        }

                        if (publishStatus is error) {
                            string errorCause = <string>publishStatus.detail()?.message;
                            string errorMessage = "Update notification failed for Topic [" + topic + "]: " + errorCause;
                            response.setTextPayload(<@untaintedstring>errorMessage);
                            log:printError(errorMessage);
                        } else {
                            log:printInfo("Update notification done for Topic [" + topic + "]");
                            response.statusCode = http:STATUS_ACCEPTED;
                            var responseError = httpCaller->respond(response);
                            if (responseError is error) {
                                log:printError("Error responding on update notification for topic[" + topic
                                + "]", responseError);
                            }
                            return;
                        }
                    } else {
                        string errorMessage = "Publish request denied for unregistered topic[" + topic + "]";
                        log:printDebug(errorMessage);
                        response.setTextPayload(<@untaintedstring>errorMessage);
                    }
                    response.statusCode = http:STATUS_BAD_REQUEST;
                    var responseError = httpCaller->respond(response);
                    if (responseError is error) {
                        log:printError("Error responding to publish request", responseError);
                    }
                } else {
                    response.statusCode = http:STATUS_BAD_REQUEST;
                    var responseError = httpCaller->respond(response);
                    if (responseError is error) {
                        log:printError("Error responding to request", responseError);
                    }
                }
            }
        }

        @http:ResourceConfig {
            methods: ["POST"],
            path: hubSubscriptionResourcePath,
            auth: hubSubscriptionResourceAuth
        }
        resource function subscribe(http:Caller httpCaller, http:Request request) {
            http:Response response = new;
            string topic = "";

            var reqFormParamMap = request.getFormParams();
            map<string> params = reqFormParamMap is map<string> ? reqFormParamMap : {};

            string mode = params[HUB_MODE] ?: "";

            var topicFromParams = params[HUB_TOPIC];
            if topicFromParams is string {
                var decodedValue = encoding:decodeUriComponent(topicFromParams, "UTF-8");
                topic = decodedValue is string ? decodedValue : topicFromParams;
            }

            if (mode != MODE_SUBSCRIBE && mode != MODE_UNSUBSCRIBE) {
                response.statusCode = http:STATUS_BAD_REQUEST;
                var responseError = httpCaller->respond(response);
                if (responseError is error) {
                    log:printError("Error responding to request", responseError);
                }
            }

            boolean validSubscriptionChangeRequest = false;
            // TODO: check the non-existing key at this point and return the 400
            var result = params[HUB_CALLBACK];
            string callbackFromParams = params[HUB_CALLBACK] ?: "";
            var decodedCallbackFromParams = encoding:decodeUriComponent(callbackFromParams, "UTF-8");
            string callback = decodedCallbackFromParams is string ? decodedCallbackFromParams : callbackFromParams;
            var validationStatus = validateSubscriptionChangeRequest(mode, topic, callback);
            if (validationStatus is error) {
                response.statusCode = http:STATUS_BAD_REQUEST;
                string errorMessage = <string>validationStatus.detail()?.message;
                response.setTextPayload(errorMessage);
            } else {
                validSubscriptionChangeRequest = true;
                response.statusCode = http:STATUS_ACCEPTED;
            }

            var responseError = httpCaller->respond(response);
            if (responseError is error) {
                log:printError("Error responding to subscription change request", responseError);
            } else if (validSubscriptionChangeRequest) {
                verifyIntentAndAddSubscription(callback, topic, params);
            }
        }
    };
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
        if (!callback.startsWith("http://") && !callback.startsWith("https://")) {
            error err = error(WEBSUB_ERROR_CODE, message = "Malformed URL specified as callback");
            return err;
        }
        if (hubTopicRegistrationRequired && !isTopicRegistered(topic)) {
            error err = error(WEBSUB_ERROR_CODE, message = "Subscription request denied for unregistered topic");
            return err;
        }
        return;
    }
    error err = error(WEBSUB_ERROR_CODE, message = "Topic/Callback cannot be null for subscription/unsubscription request");
    return err;
}

# Function to initiate intent verification for a valid subscription/unsubscription request received.
#
# + callback - The callback URL of the new subscription/unsubscription request
# + topic - The topic specified in the new subscription/unsubscription request
# + params - Parameters specified in the new subscription/unsubscription request
function verifyIntentAndAddSubscription(string callback, string topic, map<string> params) {
    http:Client callbackEp = getSubcriberCallbackClient(callback);
    string mode = params[HUB_MODE] ?: "";
    string strLeaseSeconds = params[HUB_LEASE_SECONDS] ?: "";
    var result = langint:fromString(strLeaseSeconds);
    int leaseSeconds = result is error ? 0 : result;

    //measured from the time the verification request was made from the hub to the subscriber from the recommendation
    int createdAt = time:currentTime().time;

    if (!(leaseSeconds > 0)) {
        leaseSeconds = hubLeaseSeconds;
    }
    string challenge = system:uuid();

    http:Request request = new;

    var decodedCallback = encoding:decodeUriComponent(callback, "UTF-8");
    string callbackToCheck = decodedCallback is error ? callback : decodedCallback;

    string queryParams = (stringutils:contains(callbackToCheck, ("?")) ? "&" : "?")
        + HUB_MODE + "=" + mode
        + "&" + HUB_TOPIC + "=" + topic
        + "&" + HUB_CHALLENGE + "=" + challenge;

    if (mode == MODE_SUBSCRIBE) {
        queryParams = queryParams + "&" + HUB_LEASE_SECONDS + "=" + leaseSeconds.toString();
    }

    var subscriberResponse = callbackEp->get(<@untainted string> queryParams, request);

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
                    subscriptionDetails.secret = params[HUB_SECRET] ?: "";
                    if (!isTopicRegistered(topic)) {
                        var registerStatus = registerTopic(topic);
                        if (registerStatus is error) {
                            string errCause = <string> registerStatus.detail()?.message;
                            log:printError("Error registering topic for subscription: " + errCause);
                        }
                    }
                    addSubscription(subscriptionDetails);
                } else {
                    removeSubscription(topic, callback);
                }

                log:printInfo("Intent verification successful for mode: [" + mode + "], for callback URL: ["
                        + callback + "]");
                if (hubPersistenceEnabled) {
                    error? res = persistSubscriptionChange(mode, subscriptionDetails);
                    if (res is error) {
                        log:printError("Error persisting subscription change", res);
                    }
                }
            }
        } else {
            error err = respStringPayload;
            string errCause = <string> err.detail()?.message;
            log:printInfo("Intent verification failed for mode: [" + mode + "], for callback URL: [" + callback
                    + "]: Error retrieving response payload: " + errCause);
        }
    } else {
        error err = subscriberResponse;
        string errCause = <string> err.detail()?.message;
        log:printError("Error sending intent verification request for callback URL: [" + callback + "]: " + errCause);
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

# Function to add/remove the persisted details of topics registered.
#
# + mode - Whether the change is for addition/removal
# + topic - The topic for which registration is changing
# + return - `error` if an error occurred while persisting the change, `()` otherwise
function persistTopicRegistrationChange(string mode, string topic) returns error? {
    HubPersistenceStore? hubStoreImpl = hubPersistenceStoreImpl;
    if (hubStoreImpl is HubPersistenceStore) {
        if (mode == MODE_REGISTER) {
            check hubStoreImpl.addTopic(topic);
        } else {
            check hubStoreImpl.removeTopic(topic);
        }
    }
}

# Function to add/change/remove the persisted subscription details.
#
# + mode - Whether the subscription change is for unsubscription/unsubscription
# + subscriptionDetails - The details of the subscription changing
# + return - `error` if an error occurred while persisting the change, `()` otherwise
function persistSubscriptionChange(string mode, SubscriptionDetails subscriptionDetails) returns error? {
    HubPersistenceStore? hubStoreImpl = hubPersistenceStoreImpl;
    if (hubStoreImpl is HubPersistenceStore) {
        if (mode == MODE_SUBSCRIBE) {
            check hubStoreImpl.addSubscription(subscriptionDetails);
        } else {
            check hubStoreImpl.removeSubscription(subscriptionDetails);
        }
    }
}

function setupOnStartup() returns error? {
    if (!hubPersistenceEnabled) {
        return;
    }
    HubPersistenceStore hubServicePersistenceImpl = <HubPersistenceStore> hubPersistenceStoreImpl;
    check addTopicRegistrationsOnStartup(hubServicePersistenceImpl);
    check addSubscriptionsOnStartup(hubServicePersistenceImpl); //TODO:verify against topics
}

function addTopicRegistrationsOnStartup(HubPersistenceStore persistenceStore) returns error? {
    string[]|error topics = persistenceStore.retrieveTopics();

    if (topics is string[]) {
        foreach string topic in topics {
            var registerStatus = registerTopic(topic, loadingOnStartUp = true);
            if (registerStatus is error) {
                string errCause = <string> registerStatus.detail()?.message;
                log:printError("Error registering retrieved topic details: " + errCause);
            }
        }
    } else {
        return HubStartupError(message = "Error retrieving persisted topics", cause = topics);
    }
}

function addSubscriptionsOnStartup(HubPersistenceStore persistenceStore) returns error? {
    SubscriptionDetails[]|error subscriptions = persistenceStore.retrieveAllSubscribers();

    if (subscriptions is SubscriptionDetails[]) {
        foreach SubscriptionDetails subscription in subscriptions {
            int time = time:currentTime().time;
            if (time - subscription.leaseSeconds > subscription.createdAt) {
                error? remResult = persistenceStore.removeSubscription(subscription);
                if (remResult is error) {
                    log:printError("Error removing expired subscription", remResult);
                }
                continue;
            }
            addSubscription(subscription);
        }
    } else {
        return HubStartupError(message = "Error retrieving persisted subscriptions", cause = subscriptions);
    }
}

# Function to fetch updates for a particular topic.
#
# + topic - The topic URL to be fetched to retrieve updates
# + return - `http:Response` indicating the response received on fetching the topic URL if successful,
#            `error` if an HTTP error occurred
function fetchTopicUpdate(string topic) returns http:Response|error {
    http:Client topicEp = new http:Client(topic, hubClientConfig);
    http:Request request = new;

    var fetchResponse = topicEp->get("", request);
    return fetchResponse;
}

# Function to distribute content to a subscriber on notification from publishers.
#
# + callback - The callback URL registered for the subscriber
# + subscriptionDetails - The subscription details for the particular subscriber
# + webSubContent - The content to be sent to subscribers
function distributeContent(string callback, SubscriptionDetails subscriptionDetails, WebSubContent webSubContent) {
    http:Client callbackEp = getSubcriberCallbackClient(callback);
    http:Request request = new;
    request.setPayload(webSubContent.payload);
    checkpanic request.setContentType(webSubContent.contentType);

    int currentTime = time:currentTime().time;
    int createdAt = subscriptionDetails.createdAt;
    int leaseSeconds = subscriptionDetails.leaseSeconds;

    if (currentTime - leaseSeconds > createdAt) {
        //TODO: introduce a separate periodic task, and modify select to select only active subs
        removeSubscription(subscriptionDetails.topic, callback);
        if (hubPersistenceEnabled) {
            error? remResult = persistSubscriptionChange(MODE_UNSUBSCRIBE, subscriptionDetails);
            if (remResult is error) {
                log:printError("Error removing expired subscription", remResult);
            }
        }
    } else {
        var result = request.getTextPayload();
        string stringPayload = result is error ? "" : result;

        if (subscriptionDetails.secret != "") {
            string xHubSignature = hubSignatureMethod + "=";
            string generatedSignature = "";
            if (stringutils:equalsIgnoreCase(SHA1, hubSignatureMethod)) { //not recommended
                generatedSignature = crypto:hmacSha1(stringPayload.toBytes(),
                    subscriptionDetails.secret.toBytes()).toBase16();
            } else if (stringutils:equalsIgnoreCase(SHA256, hubSignatureMethod)) {
                generatedSignature = crypto:hmacSha256(stringPayload.toBytes(),
                    subscriptionDetails.secret.toBytes()).toBase16();
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
                log:printDebug("Content delivery to callback[" + callback + "] successful for topic["
                                    + subscriptionDetails.topic + "]");
            } else if (respStatusCode == http:STATUS_GONE) {
                removeSubscription(subscriptionDetails.topic, callback);
                if (hubPersistenceEnabled) {
                    error? remResult = persistSubscriptionChange(MODE_UNSUBSCRIBE, subscriptionDetails);
                    if (remResult is error) {
                        log:printError("Error removing gone subscription", remResult);
                    }
                }
                log:printInfo("HTTP 410 response code received: Subscription deleted for callback[" + callback
                                + "], topic[" + subscriptionDetails.topic + "]");
            } else {
                log:printError("Error delivering content to callback[" + callback + "] for topic["
                            + subscriptionDetails.topic + "]: received response code " + respStatusCode.toString());
            }
        } else {
            error err = contentDistributionResponse;
            string errCause = <string> err.detail()?.message;
            log:printError("Error delivering content to callback[" + callback + "] for topic["
                            + subscriptionDetails.topic + "]: " + errCause);
        }
    }
    return;
}

# Function to retrieve cached subscriberCallbackClient for a given callback.
#
# + callback - The callback URL registered for the subscriber
# + return - `http:Client` indicating the client for a given callback from cache or new client
function getSubcriberCallbackClient(string callback) returns http:Client {
    http:Client subscriberCallbackClient;
    if (subscriberCallbackClientCache.hasKey(callback)) {
        return <http:Client>subscriberCallbackClientCache.get(callback);
    } else {
        lock {
            if (subscriberCallbackClientCache.hasKey(callback)) {
                return <http:Client>subscriberCallbackClientCache.get(callback);
            }
            subscriberCallbackClient = new http:Client(callback, hubClientConfig);
            subscriberCallbackClientCache.put(callback, <@untainted> subscriberCallbackClient);
            return subscriberCallbackClient;
        }
    }
}

// TODO: validate if no longer necessary
# Struct to represent a topic registration.
#
# + topic - The topic for which notification would happen
type TopicRegistration record {|
    string topic = "";
|};

# Object to represent a pending subscription/unsubscription request.
#
# + mode - Whether a pending subscription or unsubscription
# + topic - The topic for which the subscription or unsubscription is pending
# + callback - The callback specified for the pending subscription or unsubscription
type PendingSubscriptionChangeRequest object {

    public string mode;
    public string topic;
    public string callback;

    public function __init(string mode, string topic, string callback) {
        self.mode = mode;
        self.topic = topic;
        self.callback = callback;
    }

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

# Construct an array of groups from the comma separed group string passed
#
# + groupString - Comma separated string of groups
# + return - Array of groups
function getArray(string groupString) returns string[] {
    string[] groupsArr = [];
    if (groupString.length() == 0) {
        return groupsArr;
    }
    return stringutils:split(groupString, ",");
}
