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
import ballerina/net.uri;

//////////////////////////////////////////
/// WebSub Subscriber Service Endpoint ///
//////////////////////////////////////////
@Description {value:"Struct representing the WebSubSubscriber Service Endpoint"}
@Field {value:"config: The configuration for the endpoint"}
@Field {value:"serviceEndpoint: The underlying HTTP service endpoint"}
public struct SubscriberServiceEndpoint {
    SubscriberServiceEndpointConfiguration config;
    http:ServiceEndpoint serviceEndpoint;
}

public struct SubscriberServiceEndpointConfiguration {
    string host;
    int port;
    http:Filter[] filters;
    http:ServiceSecureSocket|null secureSocket;
    //TODO: include header, topic-resource map
}

public function <SubscriberServiceEndpointConfiguration config> SubscriberServiceEndpointConfiguration() {
    SignatureValidationFilter webSubRequestValidationFilter = { filterRequest:interceptWebSubRequest };
    http:Filter wsrHttpFilter = <http:Filter> webSubRequestValidationFilter;
    config.filters = [wsrHttpFilter];
}

public function <SubscriberServiceEndpoint ep> SubscriberServiceEndpoint() {
    ep.serviceEndpoint = {};
}

@Description {value:"Gets called when the endpoint is being initialized during package init"}
@Param {value:"config: The HTTP ServiceEndpointConfiguration of the endpoint"}
public function <SubscriberServiceEndpoint ep> init (SubscriberServiceEndpointConfiguration config) {
    http:ServiceEndpointConfiguration serviceConfig = { host:config.host, port:config.port,
                                                          secureSocket:config.secureSocket, filters:config.filters };
    ep.serviceEndpoint.init(serviceConfig);
    ep.initWebSubSubscriberServiceEndpoint();
}

public native function <SubscriberServiceEndpoint ep> initWebSubSubscriberServiceEndpoint();

@Description {value:"Gets called whenever a service attaches itself to this endpoint and during package init"}
@Param {value:"serviceType: The service attached"}
public function <SubscriberServiceEndpoint ep> register (typedesc serviceType) {
    ep.serviceEndpoint.register(serviceType);
    ep.registerWebSubSubscriberServiceEndpoint(serviceType);
}

public native function <SubscriberServiceEndpoint ep> registerWebSubSubscriberServiceEndpoint(typedesc serviceType);

@Description {value:"Starts the registered service"}
public function <SubscriberServiceEndpoint ep> start () {
    ep.serviceEndpoint.start();//TODO:not needed?
    ep.startWebSubSubscriberServiceEndpoint();
    ep.sendSubscriptionRequest();
}

@Description {value:"Native function to start the registered WebSub Subscriber service"}
native function <SubscriberServiceEndpoint ep> startWebSubSubscriberServiceEndpoint();

@Description {value:"Sends a subscription request to the specified hub if specified to subscribe on startup"}
function <SubscriberServiceEndpoint ep> sendSubscriptionRequest () {
    map subscriptionDetails = ep.retrieveSubscriptionParameters();
    if (lengthof subscriptionDetails.keys() == 0) {
        return;
    }

    string strSubscribeOnStartUp = <string> subscriptionDetails["subscribeOnStartUp"];
    boolean subscribeOnStartUp = <boolean> strSubscribeOnStartUp;

    if (subscribeOnStartUp) {
        string resourceUrl = <string> subscriptionDetails["resourceUrl"];
        string hub = <string> subscriptionDetails["hub"];
        string topic = <string> subscriptionDetails["topic"];
        if (hub == "" || topic == "") {
            if (resourceUrl == "") {
                log:printError("Subscription Request not sent since hub and/or topic and resource URL are unavailable");
                return;
            }
            match (retrieveHubAndTopicUrl(resourceUrl)) {
                (string, string) discoveredDetails => {
                    var (retHub, retTopic) = discoveredDetails;
                    retHub =? uri:decode(retHub, "UTF-8");
                    retTopic =? uri:decode(retTopic, "UTF-8");
                    subscriptionDetails["hub"] = retHub;
                    hub = retHub;
                    subscriptionDetails["topic"] = retTopic;
                    ep.setTopic(retTopic);
                }
                WebSubError websubError => {
                    log:printError("Error sending out subscription request on start up: " + websubError.errorMessage);
                    return;
                }
            }
        }
        invokeClientConnectorForSubscription(hub, subscriptionDetails);
    }
}

@Description {value:"The function called to discover hub and topic URLs defined by a resource URL"}
@Param {value:"resourceUrl: The resource URL advertising hub and topic URLs"}
@Return {value:"The (hub, topic) URLs if successful, WebSubError if not"}
function retrieveHubAndTopicUrl (string resourceUrl) returns @tainted (string, string)|WebSubError {
    endpoint http:ClientEndpoint resourceEP {targets:[{url:resourceUrl}]};
    http:Request request = {};
    var discoveryResponse = resourceEP -> get("", request);
    WebSubError websubError = {};
    match (discoveryResponse) {
        http:Response response => {
            int responseStatusCode = response.statusCode;
            if (responseStatusCode == 301 || responseStatusCode == 302) {
                return retrieveHubAndTopicUrl(response.getHeader("Location"));
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
                                websubError = { errorMessage:"Link Header contains >1 self URLs" };
                            } else {
                                topic = url;
                            }
                        }
                    }
                }
                if (hub != "" && topic != "") {
                    return (hub, topic);
                } else {
                    websubError = { errorMessage:"Hub and/or Topic URL(s) not identified in link header of resource "
                                                 + "URL[" + resourceUrl + "]" };
                }
            } else {
                websubError = { errorMessage:"Link header unavailable for resource URL[" + resourceUrl + "]" };
            }
        }
        http:HttpConnectorError connErr => {
            websubError = { errorMessage:"Error occurred with WebSub discovery for Resource URL ["
                                                     + resourceUrl + "]: " + connErr.message};
        }
    }
    return websubError;
}

@Description {value:"Sets the topic to which this service is subscribing, for auto intent verification"}
native function <SubscriberServiceEndpoint ep> setTopic (string topic);

@Description {value:"Retrieves the parameters specified for subscription as annotations and the callback URL to which
notification should happen"}
native function <SubscriberServiceEndpoint ep> retrieveSubscriptionParameters () returns (map);

@Description {value:"Returns the connector that client code uses"}
@Return {value:"The connector that client code uses"}
public function <SubscriberServiceEndpoint ep> getClient () returns (http:Connection) {
    return ep.serviceEndpoint.getClient();
}

@Description {value:"Stops the registered service"}
public function <SubscriberServiceEndpoint ep> stop () {
    ep.serviceEndpoint.stop();
}

@Description {value:"Signature validation filter for WebSub services"}
public struct SignatureValidationFilter {
    function (http:Request request, http:FilterContext context) returns (http:FilterResult) filterRequest;
    function (http:Response response, http:FilterContext context) returns (http:FilterResult) filterResponse;
}

@Description {value:"Initializes the signature validation filter for WebSub services"}
public function <SignatureValidationFilter filter> init () {
    log:printInfo("Initializing WebSub signature validation filter");
}

@Description {value:"Terminates the signature validation filter for WebSub services"}
public function <SignatureValidationFilter filter> terminate () {
    log:printInfo("Terminating WebSub signature validation filter");
}

@Description {value:"The function called to validate signature for content received by WebSub services"}
@Param {value:"request: The request being intercepted"}
@Param {value:"context: The filter context"}
@Return {value:"The result of the filter indicating whether or not proceeding can be allowed"}
public function interceptWebSubRequest (http:Request request, http:FilterContext context) returns (http:FilterResult) {
    http:FilterResult filterResult = {};
    if (request.method == "POST") {
        var processedNotification = processWebSubNotification(request, context.serviceType);
        match (processedNotification) {
            WebSubError webSubError => { filterResult =
                             {canProceed:false, statusCode:200, message:"validation failed for notification"};
            }
            //temp --> becomes null
            int | null => { filterResult =
                            {canProceed:true, statusCode:200, message:"validation successful for notification"};
            }
        }
    } else {
        filterResult = {canProceed:true, statusCode:200, message:"allow intent verification"};
    }
    return filterResult;
}

@Description {value:"Function to invoke the WebSubSubscriberConnector's actions for subscription"}
@Param {value:"hub: The hub to which the subscription request is to be sent"}
@Param {value:"subscriptionDetails: Map containing subscription details"}
function invokeClientConnectorForSubscription (string hub, map subscriptionDetails) {
    endpoint HubClientEndpoint websubHubClientEP { url:hub };

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
                                                    + "]: " + webSubError.errorMessage);
        }
    }
}
