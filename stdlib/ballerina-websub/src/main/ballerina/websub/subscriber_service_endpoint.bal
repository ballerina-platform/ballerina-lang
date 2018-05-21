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

    public {
        SubscriberServiceEndpointConfiguration config;
    }

    private {
        http:Listener serviceEndpoint;
    }

    public new () {
        http:Listener httpEndpoint = new;
        self.serviceEndpoint = httpEndpoint;
    }

    documentation {
         Gets called when the endpoint is being initialized during package initialization.
         
         P{{config}} The Subscriber Service Endpoint Configuration of the endpoint
    }
    public function init(SubscriberServiceEndpointConfiguration config);

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

    native function initWebSubSubscriberServiceEndpoint();

    native function registerWebSubSubscriberServiceEndpoint(typedesc serviceType);

    documentation {
        Sends subscription requests to the specified/discovered hubs if specified to subscribe on startup.
    }
    function sendSubscriptionRequests();

    documentation {
        Start the registered WebSub Subscriber service.
    }
    native function startWebSubSubscriberServiceEndpoint();

    documentation {
        Sets the topic to which this service is subscribing, for auto intent verification.

        P{{webSubServiceName}} The name of the service for which subscription happened for a topic
        P{{topic}} The topic the subscription happened for
    }
    native function setTopic(string webSubServiceName, string topic);

    documentation {
        Retrieves the parameters specified for subscription as annotations and the callback URL to which notification
        should happen for the services bound to the endpoint.

        R{{}} `map[]` array of maps containing subscription details for each service
    }
    native function retrieveSubscriptionParameters() returns map[];

};

public function Listener::init(SubscriberServiceEndpointConfiguration config) {
    self.config = config;
    SignatureValidationFilter sigValFilter;
    http:Filter[] filters = [<http:Filter>sigValFilter];
    http:ServiceEndpointConfiguration serviceConfig = {
        host:config.host, port:config.port, secureSocket:config.secureSocket, filters:filters
    };

    self.serviceEndpoint.init(serviceConfig);
    self.initWebSubSubscriberServiceEndpoint();
}

public function Listener::register(typedesc serviceType) {
    self.registerWebSubSubscriberServiceEndpoint(serviceType);
}

public function Listener::start() {
    self.startWebSubSubscriberServiceEndpoint();
    self.sendSubscriptionRequests();
}

public function Listener::getCallerActions() returns http:Connection {
    return self.serviceEndpoint.getCallerActions();
}

public function Listener::stop() {
    self.serviceEndpoint.stop();
}

function Listener::sendSubscriptionRequests() {
    map[] subscriptionDetailsArray = self.retrieveSubscriptionParameters();

    foreach subscriptionDetails in subscriptionDetailsArray {
        if (lengthof subscriptionDetails.keys() == 0) {
            next;
        }

        string strSubscribeOnStartUp = <string>subscriptionDetails["subscribeOnStartUp"];
        boolean subscribeOnStartUp = <boolean>strSubscribeOnStartUp;

        if (subscribeOnStartUp) {
            string resourceUrl = <string>subscriptionDetails["resourceUrl"];
            string hub = <string>subscriptionDetails["hub"];
            string topic = <string>subscriptionDetails["topic"];

            http:SecureSocket? secureSocket;
            match (<http:SecureSocket>subscriptionDetails["secureSocket"]) {
                http:SecureSocket httpSecureSocket => { secureSocket = httpSecureSocket; }
                error => { secureSocket = (); }
            }

            http:AuthConfig? auth;
            match (<http:AuthConfig>subscriptionDetails["auth"]) {
                http:AuthConfig httpAuth => { auth = httpAuth; }
                error => { auth = (); }
            }

            http:FollowRedirects? followRedirects;
            match (<http:FollowRedirects>subscriptionDetails["followRedirects"]) {
                http:FollowRedirects httpFollowRedirects => { followRedirects = httpFollowRedirects; }
                error => { followRedirects = (); }
            }

            if (hub == "" || topic == "") {
                if (resourceUrl == "") {
                    log:printError(
                        "Subscription Request not sent since hub and/or topic and resource URL are unavailable");
                    return;
                }
                match (retrieveHubAndTopicUrl(resourceUrl, auth, secureSocket, followRedirects)) {
                    (string, string) discoveredDetails => {
                        var (retHub, retTopic) = discoveredDetails;
                        retHub = check http:decode(retHub, "UTF-8");
                        retTopic = check http:decode(retTopic, "UTF-8");
                        subscriptionDetails["hub"] = retHub;
                        hub = retHub;
                        subscriptionDetails["topic"] = retTopic;
                        string webSubServiceName = <string>subscriptionDetails["webSubServiceName"];
                        self.setTopic(webSubServiceName, retTopic);
                    }
                    error websubError => {
                        log:printError("Error sending out subscription request on start up: " + websubError.message);
                        next;
                    }
                }
            }
            invokeClientConnectorForSubscription(hub, auth, secureSocket, followRedirects, subscriptionDetails);
        }
    }
}

documentation {
    Object representing the configuration for the WebSub Subscriber Service Endpoint.

    F{{host}} The configuration for the endpoint
    F{{port}} The underlying HTTP service endpoint
    F{{secureSocket}} The SSL configurations for the service endpoint
    F{{topicIdentifier}} The identifier based on which dispatching should happen for custom subscriber services
    F{{topicHeader}} The header to consider if required with dispatching for custom services
    F{{topicPayloadKeys}} The payload keys to consider if required with dispatching for custom services
    F{{topicResourceMap}} The mapping between topics and resources if required for custom services
}
public type SubscriberServiceEndpointConfiguration {
    string host;
    int port;
    http:ServiceSecureSocket? secureSocket;
    TopicIdentifier? topicIdentifier;
    string? topicHeader;
    string[]? topicPayloadKeys;
    map<map<string>>? topicResourceMap;
};

documentation {
    The function called to discover hub and topic URLs defined by a resource URL.

    P{{resourceUrl}} The resource URL advertising hub and topic URLs
    R{{}} `(string, string)` (hub, topic) URLs if successful, `error` if not
}
function retrieveHubAndTopicUrl(string resourceUrl, http:AuthConfig? auth, http:SecureSocket? secureSocket,
                                http:FollowRedirects? followRedirects) returns @tainted (string, string)|error {

    endpoint http:Client resourceEP {
        url:resourceUrl,
        auth:auth,
        secureSocket:secureSocket,
        followRedirects:followRedirects
    };

    http:Request request = new;
    var discoveryResponse = resourceEP->get("", request = request);
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
    Signature validation filter for WebSub services.
}
public type SignatureValidationFilter object {

    documentation {
        Represents the filtering function that will be invoked on WebSub notification requests.

        P{{request}} The request being intercepted
        P{{context}} The filter context
        R{{}} `http:FilterResult` The result of the filter indicating whether or not proceeding can be allowed
    }
    public function filterRequest(http:Request request, http:FilterContext context) returns http:FilterResult {
        return interceptWebSubRequest(request, context);
    }
};

//TODO: check if this can be not public
documentation {
    The function called to validate signature for content received by WebSub services.

    P{{request}} The request being intercepted
    P{{context}} The filter context
    R{{}} `http:FilterResult` The result of the filter indicating whether or not proceeding can be allowed
}
public function interceptWebSubRequest(http:Request request, http:FilterContext context) returns http:FilterResult {
    if (request.method == "POST") {
        var processedNotification = processWebSubNotification(request, context.serviceType);
        match (processedNotification) {
            error webSubError => {
                log:printDebug("Signature Validation failed for Notification: " + webSubError.message);
                http:FilterResult filterResult =
                {canProceed:false, statusCode:404, message:"validation failed for notification"};
                return filterResult;
            }
            () => {
                http:FilterResult filterResult =
                {canProceed:true, statusCode:200, message:"validation successful for notification"};
                return filterResult;
            }
        }
    } else {
        http:FilterResult filterResult = {canProceed:true, statusCode:200, message:"allow intent verification"};
        return filterResult;
    }
}

documentation {
    Function to invoke the WebSubSubscriberConnector's actions for subscription.

    P{{hub}} The hub to which the subscription request is to be sent
    P{{subscriptionDetails}} Map containing subscription details
}
function invokeClientConnectorForSubscription(string hub, http:AuthConfig? auth, http:SecureSocket? secureSocket,
                                              http:FollowRedirects? followRedirects, map subscriptionDetails) {
    endpoint Client websubHubClientEP {
        url:hub,
        secureSocket:secureSocket,
        auth:auth,
        followRedirects:followRedirects
    };

    string topic = <string>subscriptionDetails["topic"];
    string callback = <string>subscriptionDetails["callback"];

    if (hub == "" || topic == "" || callback == "") {
        log:printError("Subscription Request not sent since hub, topic and/or callback not specified");
        return;
    }

    int leaseSeconds;

    string strLeaseSeconds = <string>subscriptionDetails["leaseSeconds"];
    match (<int>strLeaseSeconds) {
        int convIntLeaseSeconds => { leaseSeconds = convIntLeaseSeconds; }
        error convError => {
            log:printError("Error retreiving specified lease seconds value: " + convError.message);
            return;
        }
    }

    string secret = <string>subscriptionDetails["secret"];

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
