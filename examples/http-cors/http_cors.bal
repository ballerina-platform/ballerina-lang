import ballerina/http;
import ballerina/log;

//Service-level CORS headers apply globally to each resource.
@http:ServiceConfig {
    cors: {
        allowOrigins: ["http://www.m3.com", "http://www.hello.com"],
        allowCredentials: false,
        allowHeaders: ["CORELATION_ID"],
        exposeHeaders: ["X-CUSTOM-HEADER"],
        maxAge: 84900
    }
}
service<http:Service> crossOriginService bind { port: 9092 } {

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
    companyInfo(endpoint caller, http:Request req) {
        http:Response res = new;
        json responseJson = { "type": "middleware" };
        res.setJsonPayload(responseJson);
        caller->respond(res) but { error e => log:printError("Failed to respond to the caller", err = e) };
    }

    //Service-level CORS headers are applied to this resource as resource-level CORS headers are not defined.
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/lang"
    }
    langInfo(endpoint caller, http:Request req) {
        http:Response res = new;
        json responseJson = { "lang": "Ballerina" };
        res.setJsonPayload(responseJson);
        caller->respond(res) but { error e => log:printError("Failed to respond to the caller", err = e) };
    }
}
