

import ballerina/http;

// token propagation is set to false by default
http:AuthProvider basicAuthProvider = {
    scheme:"basic",
    authStoreProvider:"config"
};

endpoint http:SecureListener listener {
    port:9090,
    authProviders:[basicAuthProvider]
};

// client will not propagate JWT
endpoint http:Client nyseEP {
    url: "http://localhost:9092"
};

@http:ServiceConfig {basePath:"/passthrough"}
service<http:Service> passthroughService bind listener {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    passthrough (endpoint caller, http:Request clientRequest) {
        var response = nyseEP -> get("/nyseStock/stocks", request = clientRequest);
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

http:AuthProvider jwtAuthProvider = {
    scheme: "jwt",
    issuer: "ballerina",
    audience: "ballerina",
    certificateAlias: "ballerina",
    trustStore: {
        path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
        password: "ballerina"
    }
};

endpoint http:SecureListener listener1 {
    port:9092,
    authProviders:[jwtAuthProvider]
};

@http:ServiceConfig {basePath:"/nyseStock"}
service<http:Service> nyseStockQuote bind listener1 {

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
