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

//////////////////////////////////////////
/// WebSub Subscriber Service Endpoint ///
//////////////////////////////////////////
documentation {
    Object representing the WebSubSubscriber Service Endpoint.

    F{{config}} - The configuration for the endpoint.
    F{{serviceEndpoint}} - The underlying HTTP service endpoint.
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
         Gets called when the endpoint is being initialized during package init.
         
         P{{config}} - The Subscriber Service Endpoint Configuration of the endpoint.
    }
    public function init(SubscriberServiceEndpointConfiguration config);

    documentation {
        Gets called whenever a service attaches itself to this endpoint and during package init.

        P{{serviceType}} - The service attached.
    }
    public function register(typedesc serviceType);

    documentation {
        Starts the registered service.
    }
    public function start();

    documentation {
        Returns the connector that client code uses.

        R{{}} - `http:Connection` The connector that client code uses
    }
    public function getCallerActions() returns (http:Connection);

    documentation {
        Stops the registered service.
    }
    public function stop();

    public native function initWebSubSubscriberServiceEndpoint();

    public native function registerWebSubSubscriberServiceEndpoint(typedesc serviceType);

    documentation {
        Sends a subscription request to the specified hub if specified to subscribe on startup.
    }
    function sendSubscriptionRequest();

    documentation {
        Native function to start the registered WebSub Subscriber service.
    }
    native function startWebSubSubscriberServiceEndpoint();

    documentation {
        Sets the topic to which this service is subscribing, for auto intent verification.

        P{{topic}} - The topic the subscription happened for.
    }
    native function setTopic (string topic);

    documentation {
        Retrieves the parameters specified for subscription as annotations and the callback URL to which notification
        should happen.
    }
    native function retrieveSubscriptionParameters () returns (map);

};

public function Listener::init(SubscriberServiceEndpointConfiguration config) {
    self.config = config;
    SignatureValidationFilter sigValFilter;
    http:Filter[] filters = [<http:Filter> sigValFilter];
    http:ServiceEndpointConfiguration serviceConfig = { host:config.host, port:config.port,
                                                          secureSocket:config.secureSocket, filters:filters };
    self.serviceEndpoint.init(serviceConfig);
    self.initWebSubSubscriberServiceEndpoint();
}

public function Listener::register(typedesc serviceType) {
    self.serviceEndpoint.register(serviceType);
    self.registerWebSubSubscriberServiceEndpoint(serviceType);
}

public function Listener::start() {
    self.serviceEndpoint.start();//TODO:not needed?
    self.startWebSubSubscriberServiceEndpoint();
    self.sendSubscriptionRequest();
}

public function Listener::getCallerActions() returns (http:Connection) {
    return self.serviceEndpoint.getCallerActions();
}

public function Listener::stop () {
    self.serviceEndpoint.stop();
}

function Listener::sendSubscriptionRequest() {
    map subscriptionDetails = self.retrieveSubscriptionParameters();
    if (lengthof subscriptionDetails.keys() == 0) {
        return;
    }

    string strSubscribeOnStartUp = <string> subscriptionDetails["subscribeOnStartUp"];
    boolean subscribeOnStartUp = <boolean> strSubscribeOnStartUp;

    if (subscribeOnStartUp) {
        string resourceUrl = <string> subscriptionDetails["resourceUrl"];
        string hub = <string> subscriptionDetails["hub"];
        string topic = <string> subscriptionDetails["topic"];
        http:SecureSocket? secureSocket;
        match (<http:SecureSocket> subscriptionDetails["secureSocket"]) {
            http:SecureSocket httpSecureSocket => { secureSocket = httpSecureSocket; }
            error => { secureSocket = (); }
        }

        if (hub == "" || topic == "") {
            if (resourceUrl == "") {
                log:printError("Subscription Request not sent since hub and/or topic and resource URL are unavailable");
                return;
            }
            match (retrieveHubAndTopicUrl(resourceUrl, secureSocket)) {
                (string, string) discoveredDetails => {
                    var (retHub, retTopic) = discoveredDetails;
                    match (http:decode(retHub, "UTF-8")) {
                        string decodedHub => retHub = decodedHub;
                        error => {}
                    }
                    match (http:decode(retTopic, "UTF-8")) {
                        string decodedTopic => retTopic = decodedTopic;
                        error => {}
                    }
                    subscriptionDetails["hub"] = retHub;
                    hub = retHub;
                    subscriptionDetails["topic"] = retTopic;
                    self.setTopic(retTopic);
                }
                WebSubError websubError => {
                    log:printError("Error sending out subscription request on start up: " + websubError.message);
                    return;
                }
            }
        }
        http:AuthConfig? auth;
        match (<http:AuthConfig> subscriptionDetails["auth"]) {
            http:AuthConfig httpAuthConfig => { auth = httpAuthConfig; }
            error => { auth = (); }
        }
        invokeClientConnectorForSubscription(hub, secureSocket, auth, subscriptionDetails);
    }
}

documentation {
    Object representing the configuration for the WebSubSubscriber Service Endpoint.

    F{{host}} - The configuration for the endpoint.
    F{{port}} - The underlying HTTP service endpoint.
    F{{secureSocket}} - The SSL configurations for the service endpoint.
    F{{topicIdentifier}} - The identifier based on which dispatching should happen for custom subscriber services.
    F{{topicHeader}} - The header to consider if required with dispatching for custom services.
    F{{topicPayloadKeys}} - The payload keys to consider if required with dispatching for custom services.
    F{{topicResourceMap}} - The mapping between topics and resources if required for custom services.
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
    P{{resourceUrl}} - The resource URL advertising hub and topic URLs.
    R{{}} - `(string, string)` (hub, topic) URLs if successful, `WebSubError` if not.
}
function retrieveHubAndTopicUrl (string resourceUrl, http:SecureSocket? secureSocket) returns @tainted
((string, string) | WebSubError) {
    endpoint http:Client resourceEP {
        url:resourceUrl,
        secureSocket: secureSocket
        //followRedirects:{enabled:true} //TODO: enable when re-direction is fixed
    };
    http:Request request = new;
    var discoveryResponse = resourceEP -> get("", request);
    WebSubError websubError = {};
    match (discoveryResponse) {
        http:Response response => {
            int responseStatusCode = response.statusCode;
            if (responseStatusCode == http:MOVED_PERMANENTLY_301 || responseStatusCode == http:FOUND_302) {
                return retrieveHubAndTopicUrl(response.getHeader("Location"), secureSocket);
            }
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
                                websubError = { message:"Link Header contains >1 self URLs" };
                            } else {
                                topic = url;
                            }
                        }
                    }
                }
                if (hub != "" && topic != "") {
                    return (hub, topic);
                } else {
                    websubError = { message:"Hub and/or Topic URL(s) not identified in link header of resource "
                                                 + "URL[" + resourceUrl + "]" };
                }
            } else {
                websubError = { message:"Link header unavailable for resource URL[" + resourceUrl + "]" };
            }
        }
        http:HttpConnectorError connErr => {
            websubError = { message:"Error occurred with WebSub discovery for Resource URL [" + resourceUrl + "]: "
                                    + connErr.message,
                            cause:connErr };
        }
    }
    return websubError;
}

documentation {
    Signature validation filter for WebSub services.
}
public type SignatureValidationFilter object {
    public function filterRequest (http:Request request, http:FilterContext context) returns http:FilterResult {
        return interceptWebSubRequest(request, context);
    }
};

documentation {
    The function called to validate signature for content received by WebSub services.

    P{{request}} - The request being intercepted.
    P{{context}} - The filter context.
    R{{}} - `http:FilterResult` The result of the filter indicating whether or not proceeding can be allowed.
}
public function interceptWebSubRequest (http:Request request, http:FilterContext context) returns (http:FilterResult) {
    if (request.method == "POST") {
        var processedNotification = processWebSubNotification(request, context.serviceType);
        match (processedNotification) {
            WebSubError webSubError => {
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

public function interceptionPlaceholder (http:Response response, http:FilterContext context)
returns (http:FilterResult) {
    http:FilterResult filterResult = {canProceed:true, statusCode:200, message:"Allowed Proceeding"};
    return filterResult;
}

documentation {
    Function to invoke the WebSubSubscriberConnector's actions for subscription.

    P{{hub}} - The hub to which the subscription request is to be sent.
    P{{subscriptionDetails}} - Map containing subscription details.
}
function invokeClientConnectorForSubscription (string hub, http:SecureSocket? secureSocket, http:AuthConfig? auth,
map subscriptionDetails) {
    endpoint Client websubHubClientEP {
        url:hub,
        secureSocket:secureSocket,
        auth:auth
    };

    string topic = <string> subscriptionDetails["topic"];
    string callback = <string> subscriptionDetails["callback"];

    if (hub == "" || topic == "" || callback == "") {
        log:printError("Subscription Request not sent since hub, topic and/or callback not specified");
        return;
    }

    int leaseSeconds;

    string strLeaseSeconds = <string> subscriptionDetails["leaseSeconds"];
    match (<int> strLeaseSeconds) {
        int convIntLeaseSeconds => { leaseSeconds = convIntLeaseSeconds; }
        error convError => {
            log:printError("Error retreiving specified lease seconds value: " + convError.message);
            return;
        }
    }

    string secret = <string> subscriptionDetails["secret"];

    SubscriptionChangeRequest subscriptionChangeRequest = {topic:topic, callback:callback, leaseSeconds:leaseSeconds,
                                                              secret:secret};

    var subscriptionResponse = websubHubClientEP -> subscribe(subscriptionChangeRequest);
    match (subscriptionResponse) {
        SubscriptionChangeResponse subscriptionChangeResponse => { log:printInfo(
                   "Subscription Request successful at Hub[" + subscriptionChangeResponse.hub +"], for Topic["
                                                             + subscriptionChangeResponse.topic + "], with Callback ["
                                                             + callback + "]");
        }
        WebSubError webSubError => { log:printError("Subscription Request failed at Hub[" + hub +"], for Topic[" + topic
                                                    + "]: " + webSubError.message);
        }
    }
}
