import ballerina/http;

endpoint http:NonListener mockEP {
    port: 9090
};

@http:ServiceConfig {
    basePath: "/hello"
}
@http:ServiceConfig {
    compression: http:COMPRESSION_AUTO
}
service<http:Service> hello bind mockEP {

    protocol (endpoint caller, http:Request req) {
        http:Response res = new;
        json connectionJson = {
            "protocol": "json"
        };
        res.statusCode = 200;
        res.setJsonPayload(untaint connectionJson);
        _ = caller -> respond(res);
    }
}
