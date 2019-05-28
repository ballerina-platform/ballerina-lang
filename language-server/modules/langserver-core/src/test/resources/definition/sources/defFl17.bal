import ballerina/auth;
import ballerina/http;

int port = 9090;

auth:ConfigAuthStoreProvider basicAuthProvider = new;
http:BasicAuthHeaderAuthnHandler basicAuthnHandler = new(basicAuthProvider);

http:ServiceSecureSocket secureSocket = {
    keyStore: {
        path: "",
        password: ""
    }
};

listener http:Listener apiListener = new(port, config = {
    auth: {
        authnHandlers: [basicAuthnHandler]
    },
    secureSocket: secureSocket
});
