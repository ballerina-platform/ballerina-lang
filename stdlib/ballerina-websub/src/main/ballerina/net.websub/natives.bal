package net.websub;

///////////////////////////////////////////////////////////////////
//////////////////// WebSub Subscriber Natives ////////////////////
///////////////////////////////////////////////////////////////////
@Description {value:"Function to retrieve annotations specified for the WebSub Subscriber Service"}
@Return {value:"SubscriberServiceConfiguration representing the annotation"}
native function retrieveAnnotations () returns (SubscriberServiceConfiguration);

@Description {value:"Function to retrieve annotations specified for the WebSub Subscriber Service"}
@Return {value:"WebSubSubscriberServiceConfiguration representing the annotation"}
native function retrieveSecret (typedesc serviceType) returns (string);

///////////////////////////////////////////////////////////////////
//////////////////// WebSub Hub Natives ///////////////////////////
///////////////////////////////////////////////////////////////////
@Description {value:"Starts up the internal Ballerina Hub"}
@Return {value:"The URL of the Hub service"}
native function startUpHubService () returns (string);

@Description {value:"Stop the Ballerina Hub, if started"}
@Param {value:"The URL of the Hub service"}
@Return {value:"True if the Ballerina Hub had been started up and was stopped now, false if the Hub had not been started
                up"}
native function stopHubService (string hubUrl) returns (boolean);

@Description {value:"Adds a new subscription for the specified topic in the Ballerina Hub"}
@Param {value:"subscriptionDetails: The details of the subscription including WebSub specifics"}
public native function addSubscription (SubscriptionDetails subscriptionDetails);

@Description {value:"Publishes an update against the topic in the Ballerina Hub"}
@Param {value:"topic: The topic for which the update should happen"}
@Param {value:"payload: The update payload"}
public native function publishToInternalHub (string topic, json payload);

@Description {value:"Removes a subscription added for the specified topic in the Ballerina Hub"}
@Param {value:"topic: The topic for which the subscription was added"}
@Param {value:"callback: The callback registered for this subscription"}
public native function removeSubscription (string topic, string callback);

///////////////////////////////////////////////////////////////////
//////////////////// WebSub Publisher Natives /////////////////////
///////////////////////////////////////////////////////////////////
@Description {value:"Publishes an update against the topic in the Ballerina Hub"}
@Param {value:"hubUrl: The URL of the Ballerina WebSub Hub as included in the WebSubHub struct"}
@Param {value:"topic: The topic for which the update should happen"}
@Param {value:"payload: The update payload"}
@Return {value:"String indicating the error, if an error occurred"}
native function validateAndPublishToInternalHub (string hubUrl, string topic, json payload) returns (string);
