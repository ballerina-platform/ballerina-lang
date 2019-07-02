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
        // Error
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/{name}/{age}"
    }
    resource function sayHello1(http:Caller caller, http:Request req, string name, string ag) {
        // Error
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/{name}{hi}/{age}"
    }
    resource function sayHello2(http:Caller caller, http:Request req, string name) {
        // no compilation error - signature path params should be a subset of annotated path expressions
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/{name}/{Name}",
        body: "naMe"
    }
    resource function sayHello3(http:Caller caller, http:Request req, int Name, string name, string naMe) {
        // no compilation error - path param case sensitivity
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "",
        body: ""
    }
    resource function sayHello4(http:Caller caller, http:Request req, string go) {

    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/{name}/{Name}",
        body: "naMe"
    }
    resource function sayHello5(http:Caller caller, http:Request req, int Name, string naMe, string name) {

    }

    @http:ResourceConfig {
        methods: ["GET"],
        body: "naMe"
    }
    resource function sayHello6(http:Caller caller, http:Request req, string go, string name) {

    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/naMe"
    }
    resource function sayHello7(http:Caller caller, http:Request req, string name) {

    }
}
