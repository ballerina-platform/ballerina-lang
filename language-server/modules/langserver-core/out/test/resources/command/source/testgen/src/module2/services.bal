import ballerina/auth;
import ballerina/http;
import ballerina/io;

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
    resource function onText(http:WebSocketCaller caller, string text, boolean finalFrame) {
        io:println("received: " + text);
    }
    resource function onClose(http:WebSocketCaller caller, int statusCode, string reason) {
    }
}

service wssService on securedListener2 {
    resource function onOpen(http:WebSocketCaller caller) {
    }
    resource function onText(http:WebSocketCaller caller, string text, boolean finalFrame) {
        io:println("received: " + text);
    }
    resource function onClose(http:WebSocketCaller caller, int statusCode, string reason) {
    }
}

auth:InboundBasicAuthProvider basicAuthProvider1 = new;
auth:InboundBasicAuthProvider basicAuthProvider2 = new;

http:BasicAuthHandler basicAuthHandler1 = new(basicAuthProvider1);
http:BasicAuthHandler basicAuthHandler2 = new(basicAuthProvider2);

listener http:Listener securedListener = new(9090, config = {
        auth: {
            authHandlers: [basicAuthHandler1]
        },
        secureSocket: {
            keyStore: {
                path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
                password: "ballerina"
            }
        }
    });

listener http:WebSocketListener securedListener2 = new(9090, config = {
        auth: {
            authHandlers: [basicAuthHandler2]
        },
        secureSocket: {
            keyStore: {
                path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
                password: "ballerina"
            }
        }
    });
