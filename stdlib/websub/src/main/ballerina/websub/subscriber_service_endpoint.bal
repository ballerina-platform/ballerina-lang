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
documentation {
    Object representing the WebSubSubscriber Service Endpoint.

    E{{}}
    F{{config}} The configuration for the endpoint
    F{{serviceEndpoint}} The underlying HTTP service endpoint
}
public type Listener object {

    public SubscriberServiceEndpointConfiguration config;

    private http:Listener serviceEndpoint;

    public new () {
        http:Listener httpEndpoint = new;
        self.serviceEndpoint = httpEndpoint;
    }

    documentation {
         Gets called when the endpoint is being initialized during package initialization.
         
         P{{c}} The Subscriber Service Endpoint Configuration of the endpoint
    }
    public function init(SubscriberServiceEndpointConfiguration c);

    documentation {
        Gets called whenever a service attaches itself to this endpoint and during package initialization.

        P{{serviceType}} The service attached
    }
    public function register(typedesc serviceType);

    documentation {
        Starts the registered service.
    }
    public function start();

    documentation {
        Returns the caller actions the client code uses.

        R{{}} `http:Connection` The connector that client code uses
    }
    public function getCallerActions() returns http:Connection;

    documentation {
        Stops the registered service.
    }
    public function stop();

    extern function initWebSubSubscriberServiceEndpoint();

    extern function registerWebSubSubscriberServiceEndpoint(typedesc serviceType);

    documentation {
        Sends subscription requests to the specified/discovered hubs if specified to subscribe on startup.
    }
    function sendSubscriptionRequests();

    documentation {
        Start the registered WebSub Subscriber service.
    }
    extern function startWebSubSubscriberServiceEndpoint();

    documentation {
        Sets the topic to which this service is subscribing, for auto intent verification.

        P{{webSubServiceName}} The name of the service for which subscription happened for a topic
        P{{topic}} The topic the subscription happened for
    }
    extern function setTopic(string webSubServiceName, string topic);

    documentation {
        Retrieves the parameters specified for subscription as annotations and the callback URL to which notification
        should happen for the services bound to the endpoint.

        R{{}} `map[]` array of maps containing subscription details for each service
    }
    extern function retrieveSubscriptionParameters() returns map[];

};

function Listener::init(SubscriberServiceEndpointConfiguration c) {
    self.config = c;
    http:ServiceEndpointConfiguration serviceConfig = {
        host: c.host, port: c.port, secureSocket: c.httpServiceSecureSocket
    };

    self.serviceEndpoint.init(serviceConfig);
    self.initWebSubSubscriberServiceEndpoint();
}

function Listener::register(typedesc serviceType) {
    self.registerWebSubSubscriberServiceEndpoint(serviceType);
}

function Listener::start() {
    self.startWebSubSubscriberServiceEndpoint();
    self.sendSubscriptionRequests();
}

function Listener::getCallerActions() returns http:Connection {
    return self.serviceEndpoint.getCallerActions();
}

function Listener::stop() {
    self.serviceEndpoint.stop();
}

function Listener::sendSubscriptionRequests() {
    map[] subscriptionDetailsArray = self.retrieveSubscriptionParameters();

    foreach subscriptionDetails in subscriptionDetailsArray {
        if (lengthof subscriptionDetails.keys() == 0) {
            continue;
        }

        string strSubscribeOnStartUp = <string>subscriptionDetails.subscribeOnStartUp;
        boolean subscribeOnStartUp = <boolean>strSubscribeOnStartUp;

        if (subscribeOnStartUp) {
            string resourceUrl = <string>subscriptionDetails.resourceUrl;
            string hub = <string>subscriptionDetails.hub;
            string topic = <string>subscriptionDetails.topic;

            http:SecureSocket? newSecureSocket;
            match (<http:SecureSocket>subscriptionDetails.secureSocket) {
                http:SecureSocket s => { newSecureSocket = s; }
                error => { newSecureSocket = (); }
            }

            http:AuthConfig? auth;
            match (<http:AuthConfig>subscriptionDetails.auth) {
                http:AuthConfig httpAuth => { auth = httpAuth; }
                error => { auth = (); }
            }

            http:FollowRedirects? followRedirects;
            match (<http:FollowRedirects>subscriptionDetails.followRedirects) {
                http:FollowRedirects httpFollowRedirects => { followRedirects = httpFollowRedirects; }
                error => { followRedirects = (); }
            }

            if (hub == "" || topic == "") {
                if (resourceUrl == "") {
                    log:printError(
                        "Subscription Request not sent since hub and/or topic and resource URL are unavailable");
                    return;
                }
                match (retrieveHubAndTopicUrl(resourceUrl, auth, newSecureSocket, followRedirects)) {
                    (string, string) discoveredDetails => {
                        var (retHub, retTopic) = discoveredDetails;
                        retHub = check http:decode(retHub, "UTF-8");
                        retTopic = check http:decode(retTopic, "UTF-8");
                        subscriptionDetails["hub"] = retHub;
                        hub = retHub;
                        subscriptionDetails["topic"] = retTopic;
                        string webSubServiceName = <string>subscriptionDetails.webSubServiceName;
                        self.setTopic(webSubServiceName, retTopic);
                    }
                    error websubError => {
                        log:printError("Error sending out subscription request on start up: " + websubError.message);
                        continue;
                    }
                }
            }
            invokeClientConnectorForSubscription(hub, auth, newSecureSocket, followRedirects, subscriptionDetails);
        }
    }
}

documentation {
    Object representing the configuration for the WebSub Subscriber Service Endpoint.

    F{{host}} The host name/IP of the endpoint
    F{{port}} The port to which the endpoint should bind to
    F{{httpServiceSecureSocket}} The SSL configurations for the service endpoint
    F{{extensionConfig}}    The extension configuration to introduce custom subscriber services (webhooks)
}
public type SubscriberServiceEndpointConfiguration record {
    string host;
    int port;
    http:ServiceSecureSocket? httpServiceSecureSocket;
    ExtensionConfig? extensionConfig;
};

documentation {
    The extension configuration to introduce custom subscriber services.

    F{{topicIdentifier}}                The identifier based on which dispatching should happen for custom subscriber
                                            services
    F{{topicHeader}}                    The header to consider if required with dispatching for custom services
    F{{headerResourceMap}}              The mapping between header value and resource details
    F{{payloadKeyResourceMap}}          The mapping between value for a particular JSON payload key and resource details
    F{{headerAndPayloadKeyResourceMap}} The mapping between values for the header and a particular JSON payload key and resource details
}
public type ExtensionConfig record {
    TopicIdentifier topicIdentifier = TOPIC_ID_HEADER;

    // TODO: make `Link` the default header and special case `Link` to extract the topic (rel="self").
    // <link href="<HUB_URL>"; rel="hub", href="<TOPIC_URL>"; rel="self"/>
    string? topicHeader;

    // e.g.,
    //  headerResourceMap = {
    //    "watch" : ("onWatch", WatchEvent),
    //    "create" : ("onCreate", CreateEvent)
    //  };
    map<(string, typedesc)>? headerResourceMap;

    // e.g.,
    //  payloadKeyResourceMap = {
    //    "eventType" : {
    //        "branch.created":  ("onBranchCreate", BranchCreatedEvent),
    //        "branch.deleted":  ("onBranchDelete", BranchDeletedEvent)
    //    }
    //  };
    map<map<(string, typedesc)>>? payloadKeyResourceMap;

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
    map<map<map<(string, typedesc)>>>? headerAndPayloadKeyResourceMap;
};

documentation {
    The function called to discover hub and topic URLs defined by a resource URL.

    P{{resourceUrl}} The resource URL advertising hub and topic URLs
    R{{}} `(string, string)` (hub, topic) URLs if successful, `error` if not
}
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
    error websubError = {};
    match (discoveryResponse) {
        http:Response response => {
            int responseStatusCode = response.statusCode;
            string[] linkHeaders;
            if (response.hasHeader("Link")) {
                linkHeaders = response.getHeaders("Link");
            }

            if (lengthof linkHeaders > 0) {
                string hub;
                string topic;
                string[] linkHeaderConstituents = [];
                if (lengthof linkHeaders == 1) {
                    linkHeaderConstituents = linkHeaders[0].split(",");
                } else {
                    linkHeaderConstituents = linkHeaders;
                }

                foreach link in linkHeaderConstituents {
                    string[] linkConstituents = link.split(";");
                    if (linkConstituents[1] != "") {
                        string url = linkConstituents[0].trim();
                        url = url.replace("<", "");
                        url = url.replace(">", "");
                        if (linkConstituents[1].contains("rel=\"hub\"") && hub == "") {
                            hub = url;
                        } else if (linkConstituents[1].contains("rel=\"self\"")) {
                            if (topic != "") {
                                websubError = {message:"Link Header contains >1 self URLs"};
                            } else {
                                topic = url;
                            }
                        }
                    }
                }
                if (hub != "" && topic != "") {
                    return (hub, topic);
                } else {
                    websubError = {message:"Hub and/or Topic URL(s) not identified in link header of resource "
                        + "URL[" + resourceUrl + "]"};
                }
            } else {
                websubError = {message:"Link header unavailable for resource URL[" + resourceUrl + "]"};
            }
        }
        error connErr => {
            websubError = {message:"Error occurred with WebSub discovery for Resource URL [" + resourceUrl + "]: "
                + connErr.message, cause:connErr};
        }
    }
    return websubError;
}

documentation {
    Function to invoke the WebSubSubscriberConnector's actions for subscription.

    P{{hub}} The hub to which the subscription request is to be sent
    P{{subscriptionDetails}} Map containing subscription details
}
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

    int leaseSeconds;

    string strLeaseSeconds = <string>subscriptionDetails.leaseSeconds;
    match (<int>strLeaseSeconds) {
        int convIntLeaseSeconds => { leaseSeconds = convIntLeaseSeconds; }
        error convError => {
            log:printError("Error retreiving specified lease seconds value: " + convError.message);
            return;
        }
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
    match (subscriptionResponse) {
        SubscriptionChangeResponse subscriptionChangeResponse => {
            log:printInfo("Subscription Request successful at Hub[" + subscriptionChangeResponse.hub +
                    "], for Topic[" + subscriptionChangeResponse.topic + "], with Callback [" + callback + "]");
        }
        error webSubError => {
            log:printError("Subscription Request failed at Hub[" + hub + "], for Topic[" + topic + "]: " +
                    webSubError.message);
        }
    }
}
