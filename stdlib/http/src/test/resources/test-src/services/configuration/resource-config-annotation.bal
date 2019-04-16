import ballerina/http;

listener http:MockListener helloEP  = new(9090);

@http:ServiceConfig {
    basePath:"/hello"
}
service helloWorldResourceConfig on helloEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    @http:ResourceConfig {
        methods:["POST"]
    }
    resource function sayHello (http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("Hello World !!!");
        checkpanic caller->respond(res);
    }
}
