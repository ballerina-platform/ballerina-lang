import ballerina/httpx;
import ballerina/iox;
//import ballerina/websub;

service httpService on new httpx:Listener(9090) {
    resource function sayHello(httpx:Caller caller, httpx:Request request) {
    }
}

service httpsService on securedListener {
    resource function sayHello(httpx:Caller caller, httpx:Request request) {
    }
}

service wsService on new httpx:WebSocketListener(9094) {
    resource function onOpen(httpx:WebSocketCaller caller) {
    }
    resource function onText(httpx:WebSocketCaller caller, string text) {
        iox:println("received: " + text);
    }
    resource function onClose(httpx:WebSocketCaller caller, int statusCode, string reason) {
    }
}

service wssService on securedListener2 {
    resource function onOpen(httpx:WebSocketCaller caller) {
    }
    resource function onText(httpx:WebSocketCaller caller, string text) {
        iox:println("received: " + text);
    }
    resource function onClose(httpx:WebSocketCaller caller, int statusCode, string reason) {
    }
}

//TODO: Enable once websub module is available for jBallerina
//service websubSubscriber on new websub:Listener(8282) {
//    resource function onIntentVerification(websub:Caller caller, websub:IntentVerificationRequest request) {
//    }
//    resource function onNotification(websub:Notification notification) {
//    }
//}

listener httpx:Listener securedListener = new (9092, {
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

listener httpx:WebSocketListener securedListener2 = new httpx:WebSocketListener(9092, {
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
