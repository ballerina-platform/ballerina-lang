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
import ballerina/runtime;
import ballerina/websub;

const string WEBSUB_TOPIC_ONE = "http://one.websub.topic.com";
const string WEBSUB_TOPIC_TWO = "http://two.websub.topic.com";
const string WEBSUB_TOPIC_THREE = "http://three.websub.topic.com";
const string WEBSUB_TOPIC_FOUR = "http://four.websub.topic.com";
const string WEBSUB_TOPIC_FIVE = "http://one.redir.topic.com";
const string WEBSUB_TOPIC_SIX = "http://two.redir.topic.com";

boolean remoteTopicRegistered = false;

websub:Hub webSubHub = startHubAndRegisterTopic();

websub:PublisherClient websubHubClientEP = new (webSubHub.publishUrl);

listener http:Listener publisherServiceEP = new http:Listener(23080);

service publisher on publisherServiceEP {
    @http:ResourceConfig {
        methods: ["GET", "HEAD"]
    }
    resource function discover(http:Caller caller, http:Request req) {
        http:Response response = new;
        // Add a link header indicating the hub and topic
        websub:addWebSubLinkHeader(response, [webSubHub.subscriptionUrl], WEBSUB_TOPIC_ONE);
        var err = caller->accepted(response);
        if (err is error) {
            log:printError("Error responding on ordering", err);
        }
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/notify/{subscriber}"
    }
    resource function notify(http:Caller caller, http:Request req, string subscriber) {
        remoteRegisterTopic();
        json jsonPayload = <json> req.getJsonPayload();
        json jsonMode = <json>jsonPayload.mode;
        string mode = jsonMode.toJsonString();
        json jsonContentType = <json>jsonPayload.content_type;
        string contentType = jsonContentType.toJsonString();

        var err = caller->accepted();
        if (err is error) {
            log:printError("Error responding on notify request", err);
        }

        if (subscriber != "skip_subscriber_check") {
            checkSubscriberAvailability(WEBSUB_TOPIC_ONE, "http://localhost:" + subscriber + "/websub");
            checkSubscriberAvailability(WEBSUB_TOPIC_ONE, "http://localhost:" + subscriber +
                    "/subscriberWithNoPathInAnnot");
            checkSubscriberAvailability(WEBSUB_TOPIC_ONE, "http://localhost:" + subscriber + "/websubThree?topic=" +
                    WEBSUB_TOPIC_ONE + "&fooVal=barVal");
        }

        if (mode == "internal") {
            var result = webSubHub.publishUpdate(WEBSUB_TOPIC_ONE, getPayloadContent(contentType, mode));
            if (result is error) {
                log:printError("Error publishing update directly", result);
            }
        } else {
            var result = websubHubClientEP->publishUpdate(WEBSUB_TOPIC_ONE, getPayloadContent(contentType, mode));
            if (result is error) {
                log:printError("Error publishing update remotely", result);
            }
        }
    }

    resource function topicInfo(http:Caller caller, http:Request req) {
        if (req.hasHeader("x-topic")) {
            string topicName = req.getHeader("x-topic");
            websub:SubscriberDetails[] details = webSubHub.getSubscribers(topicName);
            var err = caller->respond(details.toString());
            if (err is error) {
                log:printError("Error responding on topicInfo request", err);
            }
        } else {
            map<string> allTopics = {};
            int index=1;
            string [] availableTopics = webSubHub.getAvailableTopics();
            foreach var topic in availableTopics {
                allTopics["Topic_" + index.toString()] = topic;
                index += 1;
            }
            json j = <json> allTopics.cloneWithType(typedesc<json>);
            var err = caller->respond(j);
            if (err is error) {
                log:printError("Error responding on topicInfo request", err);
            }
        }
    }

    resource function unsubscribe(http:Caller caller, http:Request req) returns error? {
        check webSubHub.removeSubscription("http://one.websub.topic.com",
                                           "http://localhost:23181/websubThree?topic=http://one.websub.topic.com&fooVal=barVal");
        var err = caller->respond("unsubscription successful");
        if (err is error) {
            log:printError("Error responding on unsubscription request", err);
        }
    }
}

service publisherTwo on publisherServiceEP {
    @http:ResourceConfig {
        methods: ["GET", "HEAD"]
    }
    resource function discover(http:Caller caller, http:Request req) {
        http:Response response = new;
        // Add a link header indicating the hub and topic
        websub:addWebSubLinkHeader(response, [webSubHub.subscriptionUrl], WEBSUB_TOPIC_FOUR);
        var err = caller->accepted(response);
        if (err is error) {
            log:printError("Error responding on ordering", err);
        }
    }

    @http:ResourceConfig {
        methods: ["POST"]
    }
    resource function notify(http:Caller caller, http:Request req) {
        checkSubscrberAvailabilityAndPublishDirectly(WEBSUB_TOPIC_THREE, "http://localhost:23383/websub",
                                                     {"action":"publish","mode":"internal-hub"});
        checkSubscrberAvailabilityAndPublishDirectly(WEBSUB_TOPIC_FOUR, "http://localhost:23383/websubTwo",
                                                     {"action":"publish","mode":"internal-hub-two"});

        var err = caller->accepted();
        if (err is error) {
            log:printError("Error responding on notify request", err);
        }
    }
}

service contentTypePublisher on publisherServiceEP {
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/notify/{port}"
    }
    resource function notify(http:Caller caller, http:Request req, string port) {
        json jsonPayload = <json> req.getJsonPayload();
        json jsonMode = <json>jsonPayload.mode;
        string mode = jsonMode.toJsonString();
        json jsonContentType = <json>jsonPayload.content_type;
        string contentType = jsonContentType.toJsonString();

        var err = caller->accepted();
        if (err is error) {
            log:printError("Error responding on notify request", err);
        }

        if (port != "skip_subscriber_check") {
            checkSubscriberAvailability(WEBSUB_TOPIC_ONE, "http://localhost:" + port + "/websub");
            checkSubscriberAvailability(WEBSUB_TOPIC_ONE, "http://localhost:" + port + "/websubTwo");
        }

        if (mode == "internal") {
            var result = webSubHub.publishUpdate(WEBSUB_TOPIC_ONE, getPayloadContent(contentType, mode));
            if (result is error) {
                log:printError("Error publishing update directly", result);
            }
        } else {
            var result = websubHubClientEP->publishUpdate(WEBSUB_TOPIC_ONE, getPayloadContent(contentType, mode));
            if (result is error) {
                log:printError("Error publishing update remotely", result);
            }
        }
    }
}

function checkSubscrberAvailabilityAndPublishDirectly(string topic, string subscriber, json payload) {
    checkSubscriberAvailability(topic, subscriber);
    var err = webSubHub.publishUpdate(topic, payload);
    if (err is error) {
        log:printError("Error publishing update directly", err);
    }
}

function startHubAndRegisterTopic() returns websub:Hub {
    websub:Hub internalHub = startWebSubHub();
    var err = internalHub.registerTopic(WEBSUB_TOPIC_ONE);
    if (err is error) {
        log:printError("Error registering topic directly", err);
    }
    err = internalHub.registerTopic(WEBSUB_TOPIC_THREE);
    if (err is error) {
        log:printError("Error registering topic directly", err);
    }
    err = internalHub.registerTopic(WEBSUB_TOPIC_FOUR);
    if (err is error) {
        log:printError("Error registering topic directly", err);
    }
    err = internalHub.registerTopic(WEBSUB_TOPIC_FIVE);
    if (err is error) {
        log:printError("Error registering topic directly", err);
    }
    err = internalHub.registerTopic(WEBSUB_TOPIC_SIX);
    if (err is error) {
        log:printError("Error registering topic directly", err);
    }
    return internalHub;
}

function startWebSubHub() returns websub:Hub {
    var result = websub:startHub(new http:Listener(23191), "/websub", "/hub",
                                 hubConfiguration = { remotePublish : { enabled : true }});
    if (result is websub:Hub) {
        return result;
    } else if (result is websub:HubStartedUpError) {
        return result.startedUpHub;
    } else {
        panic result;
    }
}

function remoteRegisterTopic()  {
    if (remoteTopicRegistered) {
        return;
    }
    var err = websubHubClientEP->registerTopic(WEBSUB_TOPIC_TWO);
    if (err is error) {
        log:printError("Error registering topic remotely", err);
    }
    remoteTopicRegistered = true;
}

function getPayloadContent(string contentType, string mode) returns string|xml|json|byte[]|io:ReadableByteChannel {
    string errorMessage = "unknown content type";
    if (contentType == "" || contentType == "json") {
        if (mode == "internal") {
            json j = {"action":"publish","mode":"internal-hub"};
            return j;
        }
        json k = {"action":"publish","mode":"remote-hub"};
        return k;
    } else if (contentType == "string") {
        if (mode == "internal") {
            return "Text update for internal Hub";
        }
        return "Text update for remote Hub";
    } else if (contentType == "xml") {
        if (mode == "internal") {
            return xml `<websub><request>Notification</request><type>Internal</type></websub>`;
        }
        return xml `<websub><request>Notification</request><type>Remote</type></websub>`;
    } else if (contentType == "byte[]" || contentType == "io:ReadableByteChannel") {
        errorMessage = "content type " + contentType + " not yet supported with WebSub tests";
    }
    error e = error(websub:WEBSUB_ERROR_CODE, message = errorMessage);
    panic e;
}

function checkSubscriberAvailability(string topic, string callback) {
    int count = 0;
    boolean subscriberAvailable = false;
    while (!subscriberAvailable && count < 60) {
        websub:SubscriberDetails[] topicDetails = webSubHub.getSubscribers(topic);
        if (isSubscriberAvailable(topicDetails, callback)) {
            return;
        }
        runtime:sleep(1000);
        count += 1;
    }
}

function isSubscriberAvailable(websub:SubscriberDetails[] topicDetails, string callback) returns boolean {
    foreach var detail in topicDetails {
        if (detail.callback == callback) {
            return true;
        }
    }
    return false;
}
