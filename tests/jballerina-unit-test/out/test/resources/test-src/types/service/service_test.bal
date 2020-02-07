import ballerina/http;

function testServiceType () returns (service) {
    service ts = HelloWorld;
    return ts;
}

@http:ServiceConfig {basePath:"/hello"}
service HelloWorld on new http:MockListener (9090) {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/helloWorld"
    }
    resource function hello (http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("Hello, World!");
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/returnError"
    }
    resource function returnError (http:Caller conn, http:Request request) returns error? {
        error e = error("Http Error");
        return e;
    }
}
