import ballerina/http;
import ballerina/io;
import ballerina/websub;

service<http:Service> httpService bind { port: 9090 } {
    sayHello(endpoint caller, http:Request request) {
    }
}

service<http:Service> httpsService bind securedListener {
    sayHello(endpoint caller, http:Request request) {
    }
}

service<http:WebSocketService> wsService bind { port: 9094 } {
    onOpen(endpoint caller) {
    }
    onText(endpoint caller, string text, boolean final) {
        io:println("received: " + text);
    }
    onClose(endpoint caller, int statusCode, string reason) {
    }
}

service<http:WebSocketService> wssService bind securedListener2 {
    onOpen(endpoint caller) {
    }
    onText(endpoint caller, string text, boolean final) {
        io:println("received: " + text);
    }
    onClose(endpoint caller, int statusCode, string reason) {
    }
}

service<websub:Service> websubSubscriber bind {port: 9092} {
    onIntentVerification(endpoint caller, websub:IntentVerificationRequest request) {
    }
    onNotification(websub:Notification notification) {
    }
}

endpoint http:Listener securedListener {
    port: 9092,
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
};

endpoint http:Listener securedListener2 {
    port: 9092,
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
};
