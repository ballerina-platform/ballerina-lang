import ballerina/http;

endpoint http:NonListener mockEP {
    port:9093
};

@http:ServiceConfig {compression: {
    enable: "AAUUTTOO",
    contentTypes:["text/plain"]
    }
}
service<http:Service> alwaysCompressWithBogusContentType bind mockEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    test1 (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setTextPayload("Hello World!!!");
        _ = conn -> respond(res);
    }
}
