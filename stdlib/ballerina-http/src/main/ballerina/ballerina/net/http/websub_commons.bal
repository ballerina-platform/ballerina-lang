package ballerina.net.http;

import ballerina.security.crypto;

///////////////////////////////////////////////////////////////////
//////////////////// WebSub Subscriber Commons ////////////////////
///////////////////////////////////////////////////////////////////
@Description {value:"Function to build intent verification response for subscription requests sent"}
@Param {value:"request: The intent verification request from the hub"}
@Return {value:"The response to the hub verifying/denying intent to subscribe"}
public function buildSubscriptionVerificationResponse(Request request) (Response) {
    WebSubSubscriberServiceConfiguration webSubSubscriberAnnotations = retrieveAnnotations();
    return buildIntentVerificationResponse(request, MODE_SUBSCRIBE, webSubSubscriberAnnotations);
}

@Description {value:"Function to build intent verification response for unsubscription requests sent"}
@Param {value:"request: The intent verification request from the hub"}
@Return {value:"The response to the hub verifying/denying intent to subscribe"}
public function buildUnsubscriptionVerificationResponse(Request request) (Response) {
    WebSubSubscriberServiceConfiguration webSubSubscriberAnnotations = retrieveAnnotations();
    return buildIntentVerificationResponse(request, MODE_UNSUBSCRIBE, webSubSubscriberAnnotations);
}

@Description { value : "Function to build intent verification response for subscription/unsubscription requests sent" }
@Param { value : "request: The intent verification request from the hub" }
@Param { value : "mode: The mode (subscription/unsubscription) for which a request was sent" }
@Return { value : "The response to the hub verifying/denying intent to subscripe/unsubscribe" }
function buildIntentVerificationResponse(Request request, string mode,
                                 WebSubSubscriberServiceConfiguration webSubSubscriberAnnotations) (Response response) {

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

@Description {value:"Function to process and extract information for requests received at the callback" }
@Param {value:"request: The request received"}
@Return {value:"WebSubNotification extracting the WebSub headers and the payload"}
@Return {value:"WebSubError, if an error occurred in extraction"}
public function processWebSubNotification(Request request)
                                (WebSubNotification webSubNotification, WebSubError webSubError) {

    WebSubSubscriberServiceConfiguration webSubSubscriberAnnotations = retrieveAnnotations();
    var secret, _ = (string) webSubSubscriberAnnotations["secret"];
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
        return;
    } else if (secret == null) {
        if (request.getHeader(X_HUB_SIGNATURE) != null) {
            log:printWarn("Ignoring " + X_HUB_SIGNATURE + " value since secret is not specified.");
        }
    } else {
        webSubError = validateSignature(xHubSignature, payload.toString(), secret);
    }

    if (webSubError == null) {
        WebSubHeaders webSubHeaders = {xHubUuid:(string)request.getHeader(X_HUB_UUID),
                                          xHubTopic:request.getHeader(X_HUB_TOPIC)};
        if (xHubSignature != null) {
            webSubHeaders.xHubSignature = xHubSignature;
        }
        webSubNotification = { webSubHeaders:webSubHeaders, payload:payload };
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

///////////////////////////////////////////////////////////////////
//////////////////// WebSub Publisher Commons /////////////////////
///////////////////////////////////////////////////////////////////
endpoint<Client> hubClientEp {
    serviceUri:"http://localhost:8080"
}

@Description {value:"Function to publish an update to a remote hub"}
@Param {value:"hub: The hub the publisher requires updates to be published to"}
@Param {value:"topic: The topic for which the update occurred"}
@Param {value:"payload: The update payload"}
public function publish (string hub, string topic, json payload) {
    hubClientEp.init("hubClientEp", {serviceUri:hub});
    hubClientEp.start();
    var hubClient = hubClientEp.getConnector();

    Response response;
    HttpConnectorError err;
    Request request = {};

    string queryParams = HUB_MODE + "=" + MODE_PUBLISH
                         + "&" + HUB_TOPIC + "=" + topic;
    request.setJsonPayload(payload);
    response, err = hubClient -> post("?" + queryParams, request);
    if (err != null) {
        log:printError("Notification failed for hub [" + hub +"] for topic [" + topic + "]");
    } else {
        log:printInfo("Notification successful for hub [" + hub +"] for topic [" + topic + "]");
    }
}