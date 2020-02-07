import ballerina/http;

listener http:MockListener helloEP  = new(9090);

@http:ServiceConfig {basePath:"/hello"}
@http:ServiceConfig {compression: {enable: http:COMPRESSION_AUTO}}
service helloWorldServiceConfig on helloEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource function sayHello (http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("Hello World!!!");
        checkpanic caller->respond(res);
    }
}
