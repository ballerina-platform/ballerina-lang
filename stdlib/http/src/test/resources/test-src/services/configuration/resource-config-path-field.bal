import ballerina/http;

listener http:MockListener helloEP  = new(9090);

@http:ServiceConfig {
    basePath:"/hello"
}
service helloWorldResourceConfig on helloEP {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/{name}/}ag"
    }
    resource function sayHello0(http:Caller caller, http:Request req) {
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/{name}/}ag/path"
    }
    resource function sayHello1(http:Caller caller, http:Request req) {
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/{name}/{ag"
    }
    resource function sayHello2(http:Caller caller, http:Request req) {

    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/{name}/{ag/path"
    }
    resource function sayHello3(http:Caller caller, http:Request req) {

    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/{name}/{"
    }
    resource function sayHello4(http:Caller caller, http:Request req) {

    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/{}"
    }
    resource function sayHello5(http:Caller caller, http:Request req) {

    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/{name}a/go"
    }
    resource function sayHello6(http:Caller caller, http:Request req) {

    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/}/go"
    }
    resource function sayHello7(http:Caller caller, http:Request req) {

    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/{name}}/go"
    }
    resource function sayHello8(http:Caller caller, http:Request req) {

    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/{{name}/go"
    }
    resource function sayHello9(http:Caller caller, http:Request req) {

    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/a{name}/go"
    }
    resource function sayHello10(http:Caller caller, http:Request req) {

    }
}
