import ballerina/http;

http:AuthProvider basicAuthProvider11 = {
    scheme: "basic",
    authStoreProvider: "config",
    propagateJwt: true,
    issuer: "ballerina",
    keyAlias: "ballerina",
    keyPassword: "ballerina",
    keyStore: {
        path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
        password: "ballerina"
    }
};

listener http:Listener listener11 = new(9192, config = {
    authProviders: [basicAuthProvider11],
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

http:Client nyseEP03 = new("http://localhost:9193", config = {
    auth: { scheme: "JWT" }
});

@http:ServiceConfig { basePath: "/passthrough" }
service passthroughService03 on listener11 {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function passthrough(http:Caller caller, http:Request clientRequest) {
        var response = nyseEP03->get("/nyseStock/stocks", message = untaint clientRequest);
        if (response is http:Response) {
            _ = caller->respond(response);
        } else {
            json errMsg = { "error": "error occurred while invoking the service" };
            _ = caller->respond(errMsg);
        }
    }
}

http:AuthProvider jwtAuthProvider03 = {
    scheme: "jwt",
    issuer: "ballerina",
    audience: "ballerina",
    certificateAlias: "ballerina",
    trustStore: {
        path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
        password: "ballerina"
    }
};

listener http:Listener listener2 = new(9193, config = {
        authProviders: [jwtAuthProvider03],
        secureSocket: {
            keyStore: {
                path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
                password: "ballerina"
            }
        }
    });

@http:ServiceConfig { basePath: "/nyseStock" }
service nyseStockQuote03 on listener2 {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/stocks"
    }
    resource function stocks(http:Caller caller, http:Request clientRequest) {
        json payload = { "exchange": "nyse", "name": "IBM", "value": "127.50" };
        _ = caller->respond(payload);
    }
}
