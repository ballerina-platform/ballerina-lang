package ballerina.net.http;

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
    HttpConnectorError connectorError;
}

///////////////////////////////////////////////////////////////////
//////////////////// WebSub Subscriber Natives ////////////////////
///////////////////////////////////////////////////////////////////
@Description {value:"Function to retrieve annotations specified for the WebSub Subscriber Service"}
@Return {value:"WebSubSubscriberServiceConfiguration representing the annotation"}
native function retrieveAnnotations () (WebSubSubscriberServiceConfiguration);

@Description {value:"Struct to represent content by the callback"}
@Field {value:"webSubHeaders: WebSub specific headers"}
@Field {value:"payload: The payload received"}
public struct WebSubNotification {
    WebSubHeaders webSubHeaders;
    json payload;
}

@Description {value:"Struct to represent WebSub specific headers"}
@Field {value:"xHubUuid: Unique ID representing the content delivery"}
@Field {value:"xHubTopic: The topic for which the content delivery happened"}
@Field {value:"xHubSignature: The signature if the subscription was created specifying a secret"}
public struct WebSubHeaders {
    string xHubUuid;
    string xHubTopic;
    string xHubSignature;
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
    Response response;
}

///////////////////////////////////////////////////////////////////
//////////////////// WebSub Hub Natives ///////////////////////////
///////////////////////////////////////////////////////////////////
@Description {value:"Starts up the Ballerina Hub"}
public native function startUpHubService () (string);

@Description {value:"Stop the Ballerina Hub, if started"}
@Return {value:"True if the Ballerina Hub had been started up and was stopped now, false if the Hub had not been started
                up"}
public native function stopHubService () (boolean);

///////////////////////////////////////////////////////////////////
//////////////////// WebSub Publisher Natives /////////////////////
///////////////////////////////////////////////////////////////////
@Description {value:"Publishes an update against the topic in the Ballerina Hub"}
@Param {value:"topic: The topic for which the update should happen"}
@Param {value:"payload: The update payload"}
public native function publishToInternalHub (string topic, json payload);
