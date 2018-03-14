package ballerina.net.http;

import ballerina.log;

public connector WebSubSubscriberClientConnector (string hub, ClientConnector httpClientConnector) {

    Request builtSubscriptionRequest;
    Response response;
    HttpConnectorError httpConnectorError;

    @Description { value : "Subscribe to the specified topic with the specified parameters, at the hub" }
    @Param { value : "subscriptionRequest: The SubscriptionChangeRequest specifying the topic to subscribe to and the
                                        parameters to use" }
    @Return { value : "SubscriptionChangeResponse including details of subscription, if the request was successful" }
    @Return { value : "WebSubErrror indicating any errors that occurred, if the request was unsuccessful" }
    action subscribe (SubscriptionChangeRequest subscriptionRequest) returns (SubscriptionChangeResponse, WebSubError) {
        builtSubscriptionRequest = buildSubscriptionChangeOutRequest(MODE_SUBSCRIBE, subscriptionRequest);
        response, httpConnectorError = httpClientConnector -> post("/", builtSubscriptionRequest);
        return processHubResponse(hub, MODE_SUBSCRIBE, subscriptionRequest.topic, response, httpConnectorError);
    }

    @Description { value:"Unsubscribe to the specified topic at the hub" }
    @Param { value : "unsubscriptionRequest: The SubscriptionChangeRequest specifying the topic to unsubscribe from" }
    @Return { value : "SubscriptionChangeResponse including details of unsubscription, if the request was successful" }
    @Return { value : "WebSubErrror indicating any errors that occurred, if the request was unsuccessful" }
    action unsubscribe (SubscriptionChangeRequest unsubscriptionRequest) returns
                                                                         (SubscriptionChangeResponse, WebSubError) {
        builtSubscriptionRequest = buildSubscriptionChangeOutRequest(MODE_UNSUBSCRIBE, unsubscriptionRequest);
        response, httpConnectorError = httpClientConnector -> post("/", builtSubscriptionRequest);
        return processHubResponse(hub, MODE_UNSUBSCRIBE, unsubscriptionRequest.topic, response, httpConnectorError);
    }

}

@Description { value: "Function to build the subscription request to subscribe at the hub" }
@Param { value : "mode: Whether the request is for subscription or unsubscription" }
@Param { value : "subscriptionChangeRequest: The SubscriptionChangeRequest specifying the topic to subscribe to and the
                                        parameters to use" }
@Return { value : "The OutRequest to send to the hub to subscribe/unsubscribe" }
function buildSubscriptionChangeOutRequest(string mode, SubscriptionChangeRequest subscriptionChangeRequest)
(Request) {
    Request request = {};
    string body = HUB_MODE + "=" + mode
                  + "&" + HUB_TOPIC + "=" + subscriptionChangeRequest.topic
                  + "&" + HUB_CALLBACK + "=" + subscriptionChangeRequest.callback;
    if (mode == MODE_SUBSCRIBE) {
        //TODO: validate secret and lease seconds
        body = body + "&" + HUB_SECRET + "=" + subscriptionChangeRequest.secret + "&" + HUB_LEASE_SECONDS + "="
               + subscriptionChangeRequest.leaseSeconds;
    }
    request.setStringPayload(body);
    request.setHeader(CONTENT_TYPE, mime:APPLICATION_FORM_URLENCODED);
    return request;
}

@Description { value : "Function to process the response from the hub on subscription/unsubscription and extract
                    required information" }
@Param { value : "hub: The hub to which the subscription/unsubscription request was sent" }
@Param { value : "mode: Whether the request was sent for subscription or unsubscription" }
@Param { value : "topic: The topic for which the subscription/unsubscription request was sent" }
@Param { value : "response: The response received from the hub" }
@Param { value : "httpConnectorError: Error, if occurred, with HTTP client connector invocation" }
@Return { value : "SubscriptionChangeResponse including details of subscription/unsubscription,
                if the request was successful" }
@Return { value : "WebSubErrror indicating any errors that occurred, if the request was unsuccessful" }
function processHubResponse(string hub, string mode, string topic, Response response,
                            HttpConnectorError httpConnectorError) (SubscriptionChangeResponse, WebSubError) {
    SubscriptionChangeResponse subscriptionChangeResponse;
    WebSubError webSubError;
    if (httpConnectorError != null) {
        string errorMessage = "Error occurred for request: Mode[" + mode + "] at Hub[" + hub +"] - "
                              + httpConnectorError.message;
        webSubError = {errorMessage:errorMessage, connectorError:httpConnectorError};
    }
    else if (response.statusCode != 202) {
        string responsePayload;
        mime:EntityError entityError;
        string errorMessage;
        responsePayload, entityError = response.getStringPayload();
        if (entityError != null) {
            errorMessage = "Error in request: Mode[" + mode + "] at Hub[" + hub +"], "
                           + "Error occurred identifying cause: " + entityError.message;
        } else {
            errorMessage = "Error in request: Mode[" + mode + "] at Hub[" + hub +"] - " + responsePayload;
        }
        webSubError = {errorMessage:errorMessage};
    } else {
        subscriptionChangeResponse = {hub:hub, topic:topic, response:response};
    }
    return subscriptionChangeResponse, webSubError;
}

endpoint<WebSubSubscriberClient> websubSubscriberClientEP {
    serviceUri: "http://localhost:8080"
}

@Description { value : "Function to invoke the WebSubSubscriberConnector's actions for subscription" }
@Param { value:"subscriptionDetails: map containing subscription details" }
function invokeClientConnectorForSubscription (map subscriptionDetails) {
    var hub, _ = (string) subscriptionDetails["hub"];
    var topic, _ = (string) subscriptionDetails["topic"];
    var callback, _ = (string) subscriptionDetails["callback"];
    if (hub == null || topic == null || callback == null) {
        log:printError("Subscription Request not sent since hub, topic and/or callback not specified");
        return;
    }

    var strLeaseSeconds, _ = (string) subscriptionDetails["leaseSeconds"];
    var leaseSeconds, _ = <int> strLeaseSeconds;
    var secret, _ = (string) subscriptionDetails["secret"];

    websubSubscriberClientEP.init("websubSubscriberClientEP", {serviceUri:hub});
    websubSubscriberClientEP.start();

    SubscriptionChangeRequest subscriptionChangeRequest = {topic:topic, callback:callback, leaseSeconds:leaseSeconds,
                                                              secret:secret};
    SubscriptionChangeResponse subscriptionChangeResponse;
    WebSubError webSubError;
    var webSubSubscriberClient = websubSubscriberClientEP.getConnector();
    subscriptionChangeResponse, webSubError = webSubSubscriberClient -> subscribe(subscriptionChangeRequest);
    if (webSubError == null) {
        log:printInfo("Subscription Request successful at Hub[" + subscriptionChangeResponse.hub +"], for Topic["
                         + subscriptionChangeResponse.topic + "]");
    } else {
        log:printError("Subscription Request failed at Hub[" + hub +"], for Topic[" + topic + "]: "
                         + webSubError.errorMessage);
    }
}
