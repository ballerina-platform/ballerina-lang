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
    resource function sayHello0(http:Caller caller, http:Request req, @http:PathParam string name,
                                @http:PathParam string age) {
        // Error
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/{name}/{age}"
    }
    resource function sayHello1(http:Caller caller, http:Request req, @http:PathParam string name,
                                @http:PathParam string ag) {
        // Error
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/{name}{hi}/{age}"
    }
    resource function sayHello2(http:Caller caller, http:Request req, @http:PathParam string name) {
        // no compilation error - signature path params should be a subset of annotated path expressions
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/{name}/{Name}"
    }
    resource function sayHello3(http:Caller caller, http:Request req, @http:PathParam int Name,
                                @http:PathParam string name, @http:BodyParam string naMe) {
        // no compilation error - path param case sensitivity
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: ""
    }
    resource function sayHello4(http:Caller caller, http:Request req, @http:PathParam string go) {

    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/{name}/{Name}"
    }
    resource function sayHello5(http:Caller caller, http:Request req, @http:PathParam int Name,
                                @http:BodyParam string naMe, @http:PathParam string name) {

    }

    @http:ResourceConfig {
        methods: ["GET"]
    }
    resource function sayHello6(http:Caller caller, http:Request req, @http:PathParam string go,
                                @http:BodyParam string name) {

    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/naMe"
    }
    resource function sayHello7(http:Caller caller, http:Request req, @http:PathParam string name) {

    }
}
