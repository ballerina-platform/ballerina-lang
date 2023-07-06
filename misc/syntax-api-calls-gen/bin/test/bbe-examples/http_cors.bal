import ballerina/http;
import ballerina/log;

// Service-level [CORS config](https://ballerina.io/swan-lake/learn/api-docs/ballerina/http/records/CorsConfig.html) applies
// globally to each `resource`.
@http:ServiceConfig {
    cors: {
        allowOrigins: ["http://www.m3.com", "http://www.hello.com"],
        allowCredentials: false,
        allowHeaders: ["CORELATION_ID"],
        exposeHeaders: ["X-CUSTOM-HEADER"],
        maxAge: 84900
    }
}
service crossOriginService on new http:Listener(9092) {

    // Resource-level [CORS config](https://ballerina.io/swan-lake/learn/api-docs/ballerina/http/records/CorsConfig.html)
    // overrides the service-level CORS headers.
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/company",
        cors: {
            allowOrigins: ["http://www.bbc.com"],
            allowCredentials: true,
            allowHeaders: ["X-Content-Type-Options", "X-PINGOTHER"]
        }
    }
    resource function companyInfo(http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"type": "middleware"};
        res.setJsonPayload(responseJson);
        var result = caller->respond(res);
        if (result is error) {
            log:printError(result.message(), result);
        }
    }

    // Since there are no resource-level CORS configs defined here, the global
    // service-level CORS configs will be applied to this resource.
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/lang"
    }
    resource function langInfo(http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"lang": "Ballerina"};
        res.setJsonPayload(responseJson);
        var result = caller->respond(res);
        if (result is error) {
            log:printError(result.message(), result);
        }
    }
}
