import ballerina/http;

endpoint http:NonListener helloEP {
    port:9090
};

@http:ServiceConfig {
    basePath:"/hello"
}
service<http:Service> helloWorldResourceConfig bind helloEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    @http:ResourceConfig {
        methods:["POST"]
    }
    sayHello (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setStringPayload("Hello World !!!");
        _ = conn -> respond(res);
    }
}
