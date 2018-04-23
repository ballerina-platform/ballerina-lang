import ballerina/http;

endpoint http:NonListener echoEP {
    port:9090
};

@http:ServiceConfig {basePath:"/listener"}
service<http:Service> echo bind echoEP {

    string serviceLevelStringVar = "sample value";

    @http:ResourceConfig {
        methods:["GET"],
        path:"/message"
    }
    echo (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setTextPayload(serviceLevelStringVar);
        _ = conn -> respond(res);
        serviceLevelStringVar = "done";
    }
}
