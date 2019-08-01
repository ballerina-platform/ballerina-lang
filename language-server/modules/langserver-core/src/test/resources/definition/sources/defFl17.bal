import ballerina/auth;
import ballerina/http;

int port = 9090;

auth:InboundBasicAuthProvider basicAuthProvider = new;
http:BasicAuthHandler basicAuthHandler = new(basicAuthProvider);

http:ServiceSecureSocket secureSocket = {
    keyStore: {
        path: "",
        password: ""
    }
};

listener http:Listener apiListener = new(port, config = {
    auth: {
        authHandlers: [basicAuthHandler]
    },
    secureSocket: secureSocket
});
