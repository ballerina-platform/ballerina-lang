import ballerina/http;

endpoint http:NonListener mockEP {
    port:9090
};

@http:ServiceConfig {basePath : "/autoCompress", compression: http:COMPRESSION_AUTO}
service<http:Service> autoCompress bind mockEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    test1 (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setStringPayload("Hello World!!!");
        _ = conn -> respond(res);
    }
}

@http:ServiceConfig {basePath : "/alwaysCompress", compression: http:COMPRESSION_ALWAYS}
service<http:Service> alwaysCompress bind mockEP {
     @http:ResourceConfig {
        methods:["GET"],
        path:"/"
        }
    test2 (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setStringPayload("Hello World!!!");
        _ = conn -> respond(res);
    }
}

@http:ServiceConfig {basePath : "/neverCompress", compression: http:COMPRESSION_NEVER}
service<http:Service> neverCompress bind mockEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    test3 (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setStringPayload("Hello World!!!");
        _ = conn -> respond(res);
    }
}

@http:ServiceConfig {basePath : "/userOverridenValue", compression: http:COMPRESSION_NEVER}
service<http:Service> userOverridenValue bind mockEP {
    @http:ResourceConfig {
            methods:["GET"],
            path:"/"
    }
    test3 (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setStringPayload("Hello World!!!");
        res.setHeader("content-encoding", "deflate");
        _ = conn -> respond(res);
    }
}
