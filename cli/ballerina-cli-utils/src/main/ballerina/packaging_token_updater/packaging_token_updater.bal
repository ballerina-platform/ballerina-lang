import ballerina/http;

endpoint http:Listener echoEP {
    port:9090
};

@http:ServiceConfig {basePath:"/listener"}
service<http:Service> echo bind echoEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/message"
    }
    echo (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setPayload("Hello World");
        _ = conn -> respond(res);
        echoEP.stop();
    }
}
