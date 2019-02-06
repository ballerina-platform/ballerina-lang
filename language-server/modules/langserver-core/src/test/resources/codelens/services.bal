import ballerina/http;
import ballerina/task;
import ballerina/io;
import ballerina/websub;
import ballerina/log;

service httpService on new http:Listener(9090) {
    resource function sayHello(http:Caller caller, http:Request req) {
    }
}

listener http:Listener securedListener = new(
                                             9092,
                                             config = {secureSocket: {
                                                 keyStore: {
                                                     path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
                                                     password: "ballerina"
                                                 },
                                                 trustStore: {
                                                     path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                                                     password: "ballerina"
                                                 }
                                             }
                                             }
);

service httpsService on securedListener {
    resource function sayHello(http:Caller caller, http:Request req) {
    }
}

service wssService on new http:WebSocketListener(9094) {
    resource function onOpen(http:WebSocketCaller caller) {
    }
    resource function onText(http:WebSocketCaller caller, string text, boolean finalFrame) {
        io:println("received: " + text);
    }
    resource function onClose(http:WebSocketCaller caller, int statusCode, string reason) {
    }
}

listener http:WebSocketListener wsEP = new(
                                           9093,
                                           config = {secureSocket: {
                                               keyStore: {
                                                   path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
                                                   password: "ballerina"
                                               },
                                               trustStore: {
                                                   path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                                                   password: "ballerina"
                                               }
                                           }
                                           }
);

service wsssService on wsEP {
    resource function onOpen(http:WebSocketCaller caller) {
    }
    resource function onText(http:WebSocketCaller caller, string text, boolean finalFrame) {
        io:println("received: " + text);
    }
    resource function onClose(http:WebSocketCaller caller, int statusCode, string reason) {
    }
}

service websubSubscriber on websubEP {
    resource function onIntentVerification(websub:Caller caller, websub:IntentVerificationRequest request) {
    }
    resource function onNotification(websub:Notification notification) {
    }
}

listener websub:Listener websubEP = new websub:Listener(8181);