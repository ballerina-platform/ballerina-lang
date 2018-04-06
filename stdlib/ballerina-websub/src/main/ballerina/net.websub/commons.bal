package ballerina.net.websub;

import ballerina/log;
import ballerina/mime;
import ballerina/http;
import ballerina/security.crypto;

@final public string HUB_CHALLENGE = "hub.challenge";
@final public string HUB_MODE = "hub.mode";
@final public string HUB_TOPIC = "hub.topic";
@final public string HUB_CALLBACK = "hub.callback";
@final public string HUB_LEASE_SECONDS = "hub.lease_seconds";
@final public string HUB_SECRET = "hub.secret";

@final public string MODE_SUBSCRIBE = "subscribe";
@final public string MODE_UNSUBSCRIBE = "unsubscribe";
@final public string MODE_PUBLISH = "publish";

@final public string X_HUB_UUID = "X-Hub-Uuid";
@final public string X_HUB_TOPIC = "X-Hub-Topic";
@final public string X_HUB_SIGNATURE = "X-Hub-Signature";

@final public string CONTENT_TYPE = "Content-Type";
@final public string SHA1 = "SHA1";
@final public string SHA256 = "SHA256";
@final public string MD5 = "MD5";

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
public function buildSubscriptionVerificationResponse(http:Request request) returns (http:Response|null) {
    SubscriberServiceConfiguration webSubSubscriberAnnotations = retrieveAnnotations();
    return buildIntentVerificationResponse(request, MODE_SUBSCRIBE, webSubSubscriberAnnotations);
}

@Description {value:"Function to build intent verification response for unsubscription requests sent"}
@Param {value:"request: The intent verification request from the hub"}
@Return {value:"The response to the hub verifying/denying intent to subscribe"}
public function buildUnsubscriptionVerificationResponse(http:Request request) returns (http:Response|null) {
    SubscriberServiceConfiguration webSubSubscriberAnnotations = retrieveAnnotations();
    return buildIntentVerificationResponse(request, MODE_UNSUBSCRIBE, webSubSubscriberAnnotations);
}

@Description { value : "Function to build intent verification response for subscription/unsubscription requests sent" }
@Param { value : "request: The intent verification request from the hub" }
@Param { value : "mode: The mode (subscription/unsubscription) for which a request was sent" }
@Return { value : "The response to the hub verifying/denying intent to subscripe/unsubscribe" }
function buildIntentVerificationResponse(http:Request request, string mode,
                        SubscriberServiceConfiguration webSubSubscriberAnnotations) returns (http:Response|null) {
    http:Response response = {};
    string topic = webSubSubscriberAnnotations.topic;
    if (topic == "") {
        log:printError("Unable to verify intent since the topic is not specified");
        return null;
    }

    map params = request.getQueryParams();
    string reqMode = <string> params[HUB_MODE];
    string challenge = <string> params[HUB_CHALLENGE];
    string reqTopic = <string> params[HUB_TOPIC];

    string reqLeaseSeconds = <string> params[HUB_LEASE_SECONDS];

    if (reqMode == mode && reqTopic == topic) {
        response = { statusCode:202 };
        response.setStringPayload(challenge);
        log:printInfo("Intent Verification agreed - Mode [" + mode + "], Topic [" + topic +"], Lease Seconds ["
                      + reqLeaseSeconds + "]");
    } else {
        response = { statusCode:404 };
        log:printWarn("Intent Verification denied - Mode [" + mode + "], Topic [" + topic +"]");
    }
    return response;

}

@Description {value:"Function to validate signature for requests received at the callback" }
@Param {value:"request: The request received"}
@Param {value:"serviceType: The type of the service for which the request was rceived"}
@Return {value:"WebSubError, if an error occurred in extraction or signature validation failed"}
public function processWebSubNotification(http:Request request, typedesc serviceType) returns WebSubError|null {
    string secret = retrieveSecret(serviceType);
    string xHubSignature;

    if (request.hasHeader(X_HUB_SIGNATURE)) {
        xHubSignature = request.getHeader(X_HUB_SIGNATURE);
    } else {
        if (secret != "") {
            WebSubError webSubError = {errorMessage:X_HUB_SIGNATURE + " header not present for subscription added" +
                                      " specifying " + HUB_SECRET};
            return webSubError;
        } else {
            return null;
        }
    }

    json payload;
    var reqJsonPayload = request.getJsonPayload(); //TODO: fix for all types
    match (reqJsonPayload) {
        json jsonPayload => { payload = jsonPayload; }
        mime:EntityError entityError => {
            WebSubError webSubError = {errorMessage:"Error extracting notification payload: " + entityError.message};
            return webSubError;
        }
    }

    if (secret == "" && xHubSignature != "") {
        log:printWarn("Ignoring " + X_HUB_SIGNATURE + " value since secret is not specified.");
        return null;
    } else {
        return validateSignature(xHubSignature, payload.toString(), secret);
    }
}

@Description {value:"Function to validate the signature header included in the notification"}
@Param {value:"payload: The string representation of the notification payload received"}
@Param {value:"secret: The secret used when subscribing"}
@Return {value:"WebSubError if an error occurs validating the signature or the signature is invalid"}
function validateSignature (string xHubSignature, string stringPayload, string secret) returns WebSubError|null {
    WebSubError webSubError = {};
    string[] splitSignature = xHubSignature.split("=");
    string method = splitSignature[0];
    string signature = xHubSignature.replace(method + "=", "");
    string generatedSignature;

    if (SHA1.equalsIgnoreCase(method)) {
        generatedSignature = crypto:getHmac(stringPayload, secret, crypto:Algorithm.SHA1);
    } else if (SHA256.equalsIgnoreCase(method)) {
        generatedSignature = crypto:getHmac(stringPayload, secret, crypto:Algorithm.SHA256);
    } else if (MD5.equalsIgnoreCase(method)) {
        generatedSignature = crypto:getHmac(stringPayload, secret, crypto:Algorithm.MD5);
    } else {
        webSubError = {errorMessage:"Unsupported signature method: " + method};
        return webSubError;
    }

    if (!signature.equalsIgnoreCase(generatedSignature)) {
        webSubError = {errorMessage:"Signature validation failed: Invalid Signature!"};
        return webSubError;
    }
    return null;
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
public function startUpBallerinaHub () returns (WebSubHub) {
    string hubUrl = startUpHubService();
    WebSubHub ballerinaWebSubHub = { hubUrl:hubUrl };
    return ballerinaWebSubHub;
}

@Description {value:"Stops the started up Ballerina Hub"}
@Param {value:"ballerinaWebSubHub: The WebSubHub struct representing the started up hub"}
@Return {value:"Boolean indicating whether the internal Ballerina Hub was stopped"}
public function <WebSubHub ballerinaWebSubHub> shutdownBallerinaHub () returns (boolean) {
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
public function addWebSubLinkHeaders (http:Response response, string[] hubs, string topic) returns (http:Response) {
    response = response == null ? {} : response;
    string hubLinkHeader = "";
    foreach hub in hubs {
        hubLinkHeader = hubLinkHeader + "<" + hub + ">; rel=\"hub\", ";
    }
    response.setHeader("Link", hubLinkHeader + "<" + topic + ">; rel=\"self\"");
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
public function <WebSubHub ballerinaWebSubHub> publishUpdate (string topic, json payload) returns
(WebSubError | null) {
    WebSubError webSubError = {};
    if (ballerinaWebSubHub.hubUrl == null) {
        webSubError = { errorMessage:"Internal Ballerina Hub not initialized or incorrectly referenced" };
        return webSubError;
    } else {
        string errorMessage = validateAndPublishToInternalHub(ballerinaWebSubHub.hubUrl, topic, payload);
        if (errorMessage != "") {
            webSubError = { errorMessage:errorMessage };
            return webSubError;
        }
    }
    return null;
}

@Description {value:"Struct to represent Subscription Details retrieved from the database"}
@Field {value:"topic: The topic for which the subscription is added"}
@Field {value:"callback: The callback specified for the particular subscription"}
@Field {value:"secret: The secret to be used for authenticated content distribution"}
@Field {value:"leaseSeconds: The lease second period specified for the particular subscription"}
@Field {value:"createdAt: The time at which the subscription was created"}
public struct SubscriptionDetails {
    string topic;
    string callback;
    string secret;
    int leaseSeconds;
    int createdAt;
}
