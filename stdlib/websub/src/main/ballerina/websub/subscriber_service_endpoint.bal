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
import ballerina/log;

//////////////////////////////////////////
/// WebSub Subscriber Service Endpoint ///
//////////////////////////////////////////
# Object representing the WebSubSubscriber Service Endpoint.
#
# + config - The configuration for the endpoint
# + serviceEndpoint - The underlying HTTP service endpoint
public type Listener object {

    public SubscriberServiceEndpointConfiguration config = {};

    private http:Listener serviceEndpoint;

    public new () {
        http:Listener httpEndpoint = new;
        self.serviceEndpoint = httpEndpoint;
    }

    # Gets called when the endpoint is being initialized during module initialization.
    #
    # + c - The Subscriber Service Endpoint Configuration of the endpoint
    public function init(SubscriberServiceEndpointConfiguration c);

    # Gets called whenever a service attaches itself to this endpoint and during module initialization.
    #
    # + serviceType - The service attached
    public function register(typedesc serviceType);

    # Starts the registered service.
    public function start();

    # Returns the caller actions the client code uses.
    #
    # + return - `http:Connection` The connector that client code uses
    public function getCallerActions() returns http:Connection;

    # Stops the registered service.
    public function stop();

    extern function initWebSubSubscriberServiceEndpoint();

    extern function registerWebSubSubscriberServiceEndpoint(typedesc serviceType);

    # Sends subscription requests to the specified/discovered hubs if specified to subscribe on startup.
    function sendSubscriptionRequests();

    # Start the registered WebSub Subscriber service.
    extern function startWebSubSubscriberServiceEndpoint();

    # Sets the topic to which this service is subscribing, for auto intent verification.
    #
    # + webSubServiceName - The name of the service for which subscription happened for a topic
    # + topic - The topic the subscription happened for
    extern function setTopic(string webSubServiceName, string topic);

    # Retrieves the parameters specified for subscription as annotations and the callback URL to which notification
    # should happen for the services bound to the endpoint.
    #
    # + return - `map[]` array of maps containing subscription details for each service
    extern function retrieveSubscriptionParameters() returns map[];

};

function Listener.init(SubscriberServiceEndpointConfiguration c) {
    self.config = c;
    http:ServiceEndpointConfiguration serviceConfig = {
        host: c.host, port: c.port, secureSocket: c.httpServiceSecureSocket
    };

    self.serviceEndpoint.init(serviceConfig);
    self.initWebSubSubscriberServiceEndpoint();
}

function Listener.register(typedesc serviceType) {
    self.registerWebSubSubscriberServiceEndpoint(serviceType);
}

function Listener.start() {
    self.startWebSubSubscriberServiceEndpoint();
    self.sendSubscriptionRequests();
}

function Listener.getCallerActions() returns http:Connection {
    return self.serviceEndpoint.getCallerActions();
}

function Listener.stop() {
    self.serviceEndpoint.stop();
}

function Listener.sendSubscriptionRequests() {
    map[] subscriptionDetailsArray = self.retrieveSubscriptionParameters();

    foreach subscriptionDetails in subscriptionDetailsArray {
        if (subscriptionDetails.keys().length() == 0) {
            continue;
        }

        string strSubscribeOnStartUp = <string>subscriptionDetails.subscribeOnStartUp;
        boolean subscribeOnStartUp = <boolean>strSubscribeOnStartUp;

        if (subscribeOnStartUp) {
            string resourceUrl = <string>subscriptionDetails.resourceUrl;
            string hub = <string>subscriptionDetails.hub;
            string topic = <string>subscriptionDetails.topic;

            http:SecureSocket? newSecureSocket;
            var secureSocket = <http:SecureSocket>subscriptionDetails.secureSocket;
            newSecureSocket = secureSocket is http:SecureSocket ? secureSocket : ();

            http:AuthConfig? auth;
            var httpAuth = <http:AuthConfig>subscriptionDetails.auth;
            auth = httpAuth is http:AuthConfig ? httpAuth : ();

            http:FollowRedirects? followRedirects;
            var httpFollowRedirects = <http:FollowRedirects>subscriptionDetails.followRedirects;
            followRedirects = httpFollowRedirects is http:FollowRedirects ? httpFollowRedirects : ();

            if (hub == "" || topic == "") {
                if (resourceUrl == "") {
                    log:printError(
                        "Subscription Request not sent since hub and/or topic and resource URL are unavailable");
                    return;
                }
                var discoveredDetails = retrieveHubAndTopicUrl(resourceUrl, auth, newSecureSocket, followRedirects);
                if (discoveredDetails is (string, string)) {
                    var (retHub, retTopic) = discoveredDetails;
                    var hubDecodeResponse = http:decode(retHub, "UTF-8");
                    if (hubDecodeResponse is string) {
                        retHub = hubDecodeResponse;
                    } else if (hubDecodeResponse is error) {
                        panic hubDecodeResponse;
                    }
                    var topicDecodeResponse = http:decode(retTopic, "UTF-8");
                    if (topicDecodeResponse is string) {
                        retTopic = topicDecodeResponse;
                    } else if (topicDecodeResponse is error) {
                        panic topicDecodeResponse;
                    }
                    subscriptionDetails["hub"] = retHub;
                    hub = retHub;
                    subscriptionDetails["topic"] = retTopic;
                    string webSubServiceName = <string>subscriptionDetails.webSubServiceName;
                    self.setTopic(webSubServiceName, retTopic);
                } else if (discoveredDetails is error) {
                    string errCause = <string> discoveredDetails.detail().message;
                    log:printError("Error sending out subscription request on start up: " + errCause);
                    continue;
                }
            }
            invokeClientConnectorForSubscription(hub, auth, newSecureSocket, followRedirects, subscriptionDetails);
        }
    }
}

# Object representing the configuration for the WebSub Subscriber Service Endpoint.
#
# + host - The host name/IP of the endpoint
# + port - The port to which the endpoint should bind to
# + httpServiceSecureSocket - The SSL configurations for the service endpoint
# + extensionConfig - The extension configuration to introduce custom subscriber services (webhooks)
public type SubscriberServiceEndpointConfiguration record {
    string host = "";
    int port = 0;
    http:ServiceSecureSocket? httpServiceSecureSocket = ();
    ExtensionConfig? extensionConfig = ();
    !...
};

# The extension configuration to introduce custom subscriber services.
#
# + topicIdentifier - The identifier based on which dispatching should happen for custom subscriber
# + topicHeader - The header to consider if required with dispatching for custom services
# + headerResourceMap - The mapping between header value and resource details
# + payloadKeyResourceMap - The mapping between value for a particular JSON payload key and resource details
# + headerAndPayloadKeyResourceMap - The mapping between values for the header and a particular JSON payload key and resource details
public type ExtensionConfig record {
    TopicIdentifier topicIdentifier = TOPIC_ID_HEADER;

    // TODO: make `Link` the default header and special case `Link` to extract the topic (rel="self").
    // <link href="<HUB_URL>"; rel="hub", href="<TOPIC_URL>"; rel="self"/>
    string? topicHeader = ();

    // e.g.,
    //  headerResourceMap = {
    //    "watch" : ("onWatch", WatchEvent),
    //    "create" : ("onCreate", CreateEvent)
    //  };
    map<(string, typedesc)>? headerResourceMap = ();

    // e.g.,
    //  payloadKeyResourceMap = {
    //    "eventType" : {
    //        "branch.created":  ("onBranchCreate", BranchCreatedEvent),
    //        "branch.deleted":  ("onBranchDelete", BranchDeletedEvent)
    //    }
    //  };
    map<map<(string, typedesc)>>? payloadKeyResourceMap = ();

    // e.g.,
    //  headerAndPayloadKeyResourceMap = {
    //    "issue_comment" : { <--- value for header
    //        "action" : { <--- payload key
    //            "created" : ("onIssueCommentCreated", IssueCommentEvent), <--- "created" - value for key "action"
    //            "edited" : ("onIssueCommentEdited", IssueCommentEvent),
    //            "deleted" : ("onIssueCommentDeleted", IssueCommentEvent)
    //        }
    //    }
    //  };
    map<map<map<(string, typedesc)>>>? headerAndPayloadKeyResourceMap = ();
    !...
};

# The function called to discover hub and topic URLs defined by a resource URL.
#
# + resourceUrl - The resource URL advertising hub and topic URLs
# + return - `(string, string)` (hub, topic) URLs if successful, `error` if not
function retrieveHubAndTopicUrl(string resourceUrl, http:AuthConfig? auth, http:SecureSocket? localSecureSocket,
                                http:FollowRedirects? followRedirects) returns @tainted (string, string)|error {

    endpoint http:Client resourceEP {
        url:resourceUrl,
        auth:auth,
        secureSocket: localSecureSocket,
        followRedirects:followRedirects
    };

    http:Request request = new;
    var discoveryResponse = resourceEP->get("", message = request);
    error websubError = error("Dummy");
    if (discoveryResponse is http:Response) {
        var topicAndHubs = extractTopicAndHubUrls(discoveryResponse);
        if (topicAndHubs is (string, string[])) {
            string topic = "";
            string[] hubs = [];
            (topic, hubs) = topicAndHubs;
            return (hubs[0], topic); // guaranteed by `extractTopicAndHubUrls` for hubs to have length > 0
        } else if (topicAndHubs is error) {
            return topicAndHubs;
        }
    } else if (discoveryResponse is error) {
        string errCause = <string> discoveryResponse.detail().message;
        map errorDetail = { message : "Error occurred with WebSub discovery for Resource URL [" +
                                resourceUrl + "]: " + errCause };
        websubError = error(WEBSUB_ERROR_CODE, errorDetail);
    }
    return websubError;
}

# Function to invoke the WebSubSubscriberConnector's actions for subscription.
#
# + hub - The hub to which the subscription request is to be sent
# + subscriptionDetails - Map containing subscription details
function invokeClientConnectorForSubscription(string hub, http:AuthConfig? auth, http:SecureSocket? localSecureSocket,
                                              http:FollowRedirects? followRedirects, map subscriptionDetails) {
    endpoint Client websubHubClientEP {
        url:hub,
        clientSecureSocket: localSecureSocket,
        auth:auth,
        followRedirects:followRedirects
    };

    string topic = <string>subscriptionDetails.topic;
    string callback = <string>subscriptionDetails.callback;

    if (hub == "" || topic == "" || callback == "") {
        log:printError("Subscription Request not sent since hub, topic and/or callback not specified");
        return;
    }

    int leaseSeconds = 0;

    string strLeaseSeconds = <string>subscriptionDetails.leaseSeconds;
    var convIntLeaseSeconds = <int>strLeaseSeconds;
    if (convIntLeaseSeconds is int) {
        leaseSeconds = convIntLeaseSeconds;
    } else if (convIntLeaseSeconds is error) {
        string errCause = <string> convIntLeaseSeconds.detail().message;
        log:printError("Error retreiving specified lease seconds value: " + errCause);
        return;
    }

    string secret = <string>subscriptionDetails.secret;

    SubscriptionChangeRequest subscriptionChangeRequest = { topic:topic, callback:callback };

    if (leaseSeconds != 0) {
        subscriptionChangeRequest.leaseSeconds = leaseSeconds;
    }
    if (secret.trim() != "") {
        subscriptionChangeRequest.secret = secret;
    }

    var subscriptionResponse = websubHubClientEP->subscribe(subscriptionChangeRequest);
    if (subscriptionResponse is SubscriptionChangeResponse) {
        log:printInfo("Subscription Request successful at Hub[" + subscriptionResponse.hub +
                "], for Topic[" + subscriptionResponse.topic + "], with Callback [" + callback + "]");
    } else if (subscriptionResponse is error) {
        string errCause = <string> subscriptionResponse.detail().message;
        log:printError("Subscription Request failed at Hub[" + hub + "], for Topic[" + topic + "]: " + errCause);
    }
}
