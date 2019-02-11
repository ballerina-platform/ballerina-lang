import ballerina/http;
import ballerina/io;
import ballerina/websub;

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

http:AuthProvider basicAuthProvider = {
    scheme: "basic",
    authStoreProvider: "config"
};

http:AuthProvider basicAuthProvider2 = {
    scheme: "basic",
    authStoreProvider: "config"
};

listener http:Listener securedListener = new(9090, config = {
    authProviders: [basicAuthProvider],
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

listener http:WebSocketListener securedListener2 = new(9090, config = {
    authProviders: [basicAuthProvider],
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});
