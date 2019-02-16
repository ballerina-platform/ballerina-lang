import ballerina/http;
import ballerina/log;

//Service-level CORS headers apply globally to each `resource`.
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

    string respErr = "Failed to respond to the caller";

    //Resource-level CORS headers override the service-level CORS headers.
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
        json responseJson = { "type": "middleware" };
        res.setJsonPayload(responseJson);
        var result = caller->respond(res);
        if (result is error) {
           log:printError(result.reason(), err = result);
        }
    }

    //Since there are no resource-level CORS headers defined here, the global
    //service-level CORS headers are applied to this resource.
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/lang"
    }
    resource function langInfo(http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = { "lang": "Ballerina" };
        res.setJsonPayload(responseJson);
        var result = caller->respond(res);
        if (result is error) {
           log:printError(result.reason(), err = result);
        }
    }
}
