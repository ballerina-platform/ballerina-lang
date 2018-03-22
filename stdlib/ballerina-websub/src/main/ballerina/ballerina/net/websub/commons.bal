package ballerina.net.websub;

import ballerina/log;
import ballerina/mime;
import ballerina/net.http;
import ballerina/security.crypto;

public const string HUB_CHALLENGE = "hub.challenge";
public const string HUB_MODE = "hub.mode";
public const string HUB_TOPIC = "hub.topic";
public const string HUB_CALLBACK = "hub.callback";
public const string HUB_LEASE_SECONDS = "hub.lease_seconds";
public const string HUB_SECRET = "hub.secret";

public const string MODE_SUBSCRIBE = "subscribe";
public const string MODE_UNSUBSCRIBE = "unsubscribe";
public const string MODE_PUBLISH = "publish";

public const string X_HUB_UUID = "X-Hub-Uuid";
public const string X_HUB_TOPIC = "X-Hub-Topic";
public const string X_HUB_SIGNATURE = "X-Hub-Signature";

public const string CONTENT_TYPE = "Content-Type";
public const string SHA1 = "SHA1";
public const string SHA256 = "SHA256";
public const string MD5 = "MD5";

@Description {value:"Struct to represent WebSub related errors"}
@Field {value:"errorMessage: Error message indicating an issue"}
@Field {value:"connectorError: HttpConnectorError if occurred"}
public struct WebSubError {
    string errorMessage;
    http:HttpConnectorError connectorError;
}

///////////////////////////////////////////////////////////////////
//////////////////// WebSub Subscriber Commons ////////////////////
///////////////////////////////////////////////////////////////////
@Description {value:"Function to build intent verification response for subscription requests sent"}
@Param {value:"request: The intent verification request from the hub"}
@Return {value:"The response to the hub verifying/denying intent to subscribe"}
public function buildSubscriptionVerificationResponse(http:Request request) (http:Response) {
    SubscriberServiceConfiguration webSubSubscriberAnnotations = retrieveAnnotations();
    return buildIntentVerificationResponse(request, MODE_SUBSCRIBE, webSubSubscriberAnnotations);
}

@Description {value:"Function to build intent verification response for unsubscription requests sent"}
@Param {value:"request: The intent verification request from the hub"}
@Return {value:"The response to the hub verifying/denying intent to subscribe"}
public function buildUnsubscriptionVerificationResponse(http:Request request) (http:Response) {
    SubscriberServiceConfiguration webSubSubscriberAnnotations = retrieveAnnotations();
    return buildIntentVerificationResponse(request, MODE_UNSUBSCRIBE, webSubSubscriberAnnotations);
}

@Description { value : "Function to build intent verification response for subscription/unsubscription requests sent" }
@Param { value : "request: The intent verification request from the hub" }
@Param { value : "mode: The mode (subscription/unsubscription) for which a request was sent" }
@Return { value : "The response to the hub verifying/denying intent to subscripe/unsubscribe" }
function buildIntentVerificationResponse(http:Request request, string mode,
                                SubscriberServiceConfiguration webSubSubscriberAnnotations) (http:Response response) {

    if (webSubSubscriberAnnotations == null || webSubSubscriberAnnotations.topic == null) {
        log:printError("Unable to verify intent since the topic is not specified");
        return;
    }

    string topic = webSubSubscriberAnnotations.topic;
    map params = request.getQueryParams();
    var reqMode, _ = (string) params[HUB_MODE];
    var challenge, _ = (string) params[HUB_CHALLENGE];
    var reqTopic, _ = (string) params[HUB_TOPIC];
    var reqLeaseSeconds, _ = (string) params[HUB_LEASE_SECONDS];

    if (reqMode == mode && reqTopic == topic) {
        response = { statusCode:202 };
        response.setStringPayload(challenge);
        log:printInfo("Intent Verification agreed - Mode [" + mode + "], Topic [" + topic +"], Lease Seconds ["
                      + reqLeaseSeconds + "]");
    } else if (reqMode == mode) {
        response = { statusCode:205 };
        log:printWarn("Intent Verification denied - Mode [" + mode + "] for Incorrect Topic [" + topic +"]");
    } else {
        response = { statusCode:404 };
        log:printWarn("Intent Verification denied - Mode [" + mode + "], Topic [" + topic +"]");
    }
    return;

}

@Description {value:"Function to validate signature for requests received at the callback" }
@Param {value:"request: The request received"}
@Param {value:"serviceType: The type of the service for which the request was rceived"}
@Return {value:"WebSubError, if an error occurred in extraction or signature validation failed"}
public function processWebSubNotification(http:Request request, typedesc serviceType) (WebSubError webSubError) {
    string secret = retrieveSecret(serviceType);
    string xHubSignature = (string) request.getHeader(X_HUB_SIGNATURE);

    json payload;
    mime:EntityError entityError;
    payload, entityError = request.getJsonPayload(); //TODO: fix for all types

    if (entityError != null) {
        webSubError = {errorMessage:"Error extracting notification payload: " + entityError.message};
        return;
    }

    if (secret != null && request.getHeader(X_HUB_SIGNATURE) == null) {
        webSubError = {errorMessage:X_HUB_SIGNATURE + " header not present for subscription added specifying "
                                    + HUB_SECRET};
    } else if (secret == null) {
        if (request.getHeader(X_HUB_SIGNATURE) != null) {
            log:printWarn("Ignoring " + X_HUB_SIGNATURE + " value since secret is not specified.");
        }
    } else {
        webSubError = validateSignature(xHubSignature, payload.toString(), secret);
    }
    return;
}

@Description {value:"Function to validate the signature header included in the notification"}
@Param {value:"payload: The string representation of the notification payload received"}
@Param {value:"secret: The secret used when subscribing"}
@Return {value:"WebSubError if an error occurs validating the signature or the signature is invalid"}
function validateSignature (string xHubSignature, string stringPayload, string secret) (WebSubError webSubError) {
    string[] splitSignature = xHubSignature.split("=");
    string method = splitSignature[0];
    string signature = xHubSignature.replace(method + "=", "");
    string generatedSignature = null;

    if (SHA1.equalsIgnoreCase(method)) {
        generatedSignature = crypto:getHmac(stringPayload, secret, crypto:Algorithm.SHA1);
    } else if (SHA256.equalsIgnoreCase(method)) {
        generatedSignature = crypto:getHmac(stringPayload, secret, crypto:Algorithm.SHA256);
    } else if (MD5.equalsIgnoreCase(method)) {
        generatedSignature = crypto:getHmac(stringPayload, secret, crypto:Algorithm.MD5);
    } else {
        webSubError = {errorMessage:"Unsupported signature method: " + method};
        return;
    }

    if (!signature.equalsIgnoreCase(generatedSignature)) {
        webSubError = {errorMessage:"Signature validation failed: Invalid Signature!"};
    }
    return;
}

@Description {value:"Struct to represent a WebSub subscription request"}
@Field {value:"topic: The topic for which the subscription/unsubscription request is sent"}
@Field {value:"callback: The callback which should be registered/unregistered for the subscription/unsubscription
                request is sent"}
@Field {value:"leaseSeconds: The lease period for which the subscription is expected to be active"}
@Field {value:"secret: The secret to be used for authenticated content distribution with this subscription"}
public struct SubscriptionChangeRequest {
    string topic;
    string callback;
    int leaseSeconds;
    string secret;
}

@Description {value:"Struct to represent subscription/unsubscription details on success"}
@Field {value:"hub: The hub at which the subscription/unsubscription was successful"}
@Field {value:"topic: The topic for which the subscription/unsubscription was successful"}
@Field {value:"response: The response from the hub to the subscription/unsubscription requests"}
public struct SubscriptionChangeResponse {
    string hub;
    string topic;
    http:Response response;
}

/////////////////////////////////////////////////////////////
//////////////////// WebSub Hub Commons /////////////////////
/////////////////////////////////////////////////////////////
@Description {value:"Starts up the Ballerina Hub"}
@Param {value:"ballerinaWebSubHub: The WebSubHub struct representing the started up hub"}
public function startUpBallerinaHub () (WebSubHub ballerinaWebSubHub) {
    string hubUrl = startUpHubService();
    ballerinaWebSubHub = { hubUrl:hubUrl };
    return;
}

@Description {value:"Stops the started up Ballerina Hub"}
@Param {value:"ballerinaWebSubHub: The WebSubHub struct representing the started up hub"}
@Return {value:"Boolean indicating whether the internal Ballerina Hub was stopped"}
public function <WebSubHub ballerinaWebSubHub> shutdownBallerinaHub () (boolean) {
    //TODO: fix to stop
    string hubUrl = ballerinaWebSubHub.hubUrl;
    return stopHubService(hubUrl);
}

///////////////////////////////////////////////////////////////////
//////////////////// WebSub Publisher Commons /////////////////////
///////////////////////////////////////////////////////////////////
@Description {value:"Function to add link headers to a response to allow WebSub discovery"}
@Param {value:"response: The response being sent"}
@Param {value:"hubs: The hubs the publisher advertises as the hubs that it publishes updates to"}
@Param {value:"topic: The topic to which subscribers need to subscribe to, to receive updates for the resource/topic"}
@Return{value:"Response with the link header added"}
public function addWebSubLinkHeaders (http:Response response, string[] hubs, string topic) (http:Response) {
    response = response == null ? {} : response;
    string hubLinkHeader = "";
    foreach hub in hubs {
        hubLinkHeader = hubLinkHeader + "<" + hub + "> ; rel=\"hub\", ";
    }
    response.setHeader("Link", hubLinkHeader + "<" + topic + "> ; rel=\"self\"");
    return response;
}

@Description {value:"Struct to represent a WebSub Hub"}
@Field {value:"hubUrl: The URL of the WebSub Hub"}
public struct WebSubHub {
    string hubUrl;
}

@Description {value:"Publishes an update against the topic in the initialized Ballerina Hub"}
@Param {value:"topic: The topic for which the update should happen"}
@Param {value:"payload: The update payload"}
@Return {value:"WebSubError if the hub is not initialized or ballerinaWebSubHub does not represent the internal hub"}
public function <WebSubHub ballerinaWebSubHub> publishUpdate (string topic, json payload)
(WebSubError webSubError) {
    if (ballerinaWebSubHub.hubUrl == null) {
        webSubError = { errorMessage:"Internal Ballerina Hub not initialized or incorrectly referenced" };
    } else {
        string errorMessage = validateAndPublishToInternalHub(ballerinaWebSubHub.hubUrl, topic, payload);
        if (errorMessage != null) {
            webSubError = { errorMessage:errorMessage };
        }
    }
    return;
}
