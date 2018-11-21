import ballerina/http;

http:AuthProvider basicAuthProvider11 = {
    scheme:"basic",
    authStoreProvider:"config",
    propagateJwt: true,
    issuer:"ballerina",
    keyAlias:"ballerina",
    keyPassword:"ballerina",
    keyStore: {
        path:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
        password:"ballerina"
    }
};

endpoint http:Listener listener11 {
    port:9192,
    authProviders:[basicAuthProvider11],
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
};

endpoint http:Client nyseEP03 {
    url: "http://localhost:9193",
    auth: {scheme: "JWT"}
};

@http:ServiceConfig {basePath:"/passthrough"}
service<http:Service> passthroughService03 bind listener11 {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    passthrough (endpoint caller, http:Request clientRequest) {
        var response = nyseEP03 -> get("/nyseStock/stocks", message = untaint clientRequest);
        match response {
            http:Response httpResponse => {
                _ = caller -> respond(httpResponse);
            }
            error err => {
                http:Response errorResponse = new;
                json errMsg = {"error":"error occurred while invoking the service"};
                errorResponse.setJsonPayload(errMsg);
                _ = caller -> respond(errorResponse);
            }
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

endpoint http:Listener listener2 {
    port:9193,
    authProviders:[jwtAuthProvider03],
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
};

@http:ServiceConfig {basePath:"/nyseStock"}
service<http:Service> nyseStockQuote03 bind listener2 {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/stocks"
    }
    stocks (endpoint caller, http:Request clientRequest) {
        http:Response res = new;
        json payload = {"exchange":"nyse", "name":"IBM", "value":"127.50"};
        res.setJsonPayload(payload);
        _ = caller -> respond(res);
    }
}
