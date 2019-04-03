import ballerina/http;

int port = 9090;

http:AuthProvider basicAuthProvider = {
    id: "",
    scheme: "BASIC_AUTH",
    authStoreProvider: "CONFIG_AUTH_STORE"
};

http:ServiceSecureSocket secureSocket = {
    keyStore: {
        path: "",
        password: ""
    }
};

listener http:Listener apiListener = new(port, config = { authProviders: [basicAuthProvider],
        secureSocket: secureSocket });
        
