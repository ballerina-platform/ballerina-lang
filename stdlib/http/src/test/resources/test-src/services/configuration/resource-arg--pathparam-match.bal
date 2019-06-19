import ballerina/http;

listener http:MockListener helloEP  = new(9090);

@http:ServiceConfig {
    basePath:"/hello"
}
service helloWorldResourceConfig on helloEP {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/{name}/{ag}"
    }
    resource function sayHello0(http:Caller caller, http:Request req, string name, string age) {

    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/{name}/{age}"
    }
    resource function sayHello1(http:Caller caller, http:Request req, string name, string age) {

    }
}
