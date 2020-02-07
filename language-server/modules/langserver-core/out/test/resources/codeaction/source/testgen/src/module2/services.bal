import ballerina/http;
import ballerina/io;
//import ballerina/websub;

service httpService on new http:Listener(9090) {
    resource function sayHello(http:Caller caller, http:Request request) {
    }
}

service httpsService on securedListener {
    resource function sayHello(http:Caller caller, http:Request request) {
    }
}

service wsService on new http:WebSocketListener(9094) {
    resource function onOpen(http:WebSocketCaller caller) {
    }
    resource function onText(http:WebSocketCaller caller, string text) {
        io:println("received: " + text);
    }
    resource function onClose(http:WebSocketCaller caller, int statusCode, string reason) {
    }
}

service wssService on securedListener2 {
    resource function onOpen(http:WebSocketCaller caller) {
    }
    resource function onText(http:WebSocketCaller caller, string text) {
        io:println("received: " + text);
    }
    resource function onClose(http:WebSocketCaller caller, int statusCode, string reason) {
    }
}

//TODO: Enable once websub module is available for jBallerina
//service websubSubscriber on new websub:Listener(8282) {
//    resource function onIntentVerification(websub:Caller caller, websub:IntentVerificationRequest request) {
//    }
//    resource function onNotification(websub:Notification notification) {
//    }
//}

listener http:Listener securedListener = new (9092, {
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        },
        trustStore: {
            path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        }
    }
});

listener http:WebSocketListener securedListener2 = new http:WebSocketListener(9092, {
    host: "0.0.0.0",
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        },
        trustStore: {
            path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        }
    }
});
