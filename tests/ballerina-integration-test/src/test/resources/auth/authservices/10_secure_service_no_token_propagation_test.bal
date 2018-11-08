import ballerina/http;
import ballerina/io;

// token propagation is set to false by default
http:AuthProvider basicAuthProvider10 = {
    scheme:"basic",
    authStoreProvider:"config"
};

endpoint http:Listener listener10_1 {
    port:9190,
    authProviders:[basicAuthProvider10],
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
};

// client will not propagate JWT
endpoint http:Client nyseEP {
    url: "https://localhost:9195"
};

@http:ServiceConfig {basePath:"/passthrough"}
service<http:Service> passthroughService bind listener10_1 {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    passthrough (endpoint caller, http:Request clientRequest) {
        var response = nyseEP -> get("/nyseStock/stocks", message = untaint clientRequest);
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

http:AuthProvider jwtAuthProvider10 = {
    scheme: "jwt",
    issuer: "ballerina",
    audience: "ballerina",
    certificateAlias: "ballerina",
    trustStore: {
        path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
        password: "ballerina"
    }
};

endpoint http:Listener listener10_2 {
    port:9195,
    authProviders:[jwtAuthProvider10],
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
};

@http:ServiceConfig {basePath:"/nyseStock"}
service<http:Service> nyseStockQuote bind listener10_2 {

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
