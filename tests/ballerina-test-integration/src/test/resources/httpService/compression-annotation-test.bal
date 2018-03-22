import ballerina/net.http;

endpoint http:ServiceEndpoint mockEP {
    port:9090
};

@http:ServiceConfig {basePath:"/autoCompress", compression: http:Compression.AUTO}
service<http:Service> autoCompress bind mockEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    test1 (endpoint conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("Hello World!!!");
        _ = conn -> respond(res);
    }
}

@http:ServiceConfig {basePath:"/alwaysCompress", compression: http:Compression.ALWAYS}
service<http:Service> alwaysCompress bind mockEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    test2 (endpoint conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("Hello World!!!");
        _ = conn -> respond(res);
    }
}

@http:ServiceConfig {basePath:"/neverCompress", compression: http:Compression.NEVER}
service<http:Service> neverCompress bind mockEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    test3 (endpoint conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("Hello World!!!");
        _ = conn -> respond(res);
    }
}

@http:ServiceConfig {basePath:"/userOverridenValue", compression: http:Compression.NEVER}
service<http:Service> userOverridenValue bind mockEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    test3 (endpoint conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("Hello World!!!");
        res.setHeader("content-encoding", "deflate");
        _ = conn -> respond(res);
    }
}
