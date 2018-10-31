import ballerina/http;

endpoint http:NonListener helloEP {
    port:9090
};

@http:ServiceConfig {basePath:"/hello"}
@http:ServiceConfig {compression: {enable: http:COMPRESSION_AUTO}}
service<http:Service> helloWorldServiceConfig bind helloEP{

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    sayHello (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setTextPayload("Hello World!!!");
        _ = conn -> respond(res);
    }
}
