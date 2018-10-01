import ballerina/http;

endpoint http:NonListener mockEP {
    port:9090
};

@http:ServiceConfig {basePath : "/autoCompress", compression: {enable: http:COMPRESSION_AUTO}}
service<http:Service> autoCompress bind mockEP {
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

@http:ServiceConfig {compression: {contentTypes:["text/plain"]}}
service<http:Service> autoCompressWithContentType bind mockEP {
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

@http:ServiceConfig {basePath : "/alwaysCompress", compression: {enable: http:COMPRESSION_ALWAYS}}
service<http:Service> alwaysCompress bind mockEP {
     @http:ResourceConfig {
        methods:["GET"],
        path:"/"
        }
    test2 (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setTextPayload("Hello World!!!");
        _ = conn -> respond(res);
    }
}

@http:ServiceConfig {compression: {enable: http:COMPRESSION_ALWAYS, contentTypes:["text/plain","Application/Json"]}}
service<http:Service> alwaysCompressWithContentType bind mockEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    test2 (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setJsonPayload({ test: "testValue" }, contentType = "application/json;charset=\"ISO_8859-1:1987\"");
        _ = conn -> respond(res);
    }
}

@http:ServiceConfig {basePath : "/neverCompress", compression: {enable: http:COMPRESSION_NEVER}}
service<http:Service> neverCompress bind mockEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    test3 (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setTextPayload("Hello World!!!");
        _ = conn -> respond(res);
    }
}

@http:ServiceConfig {compression: {enable: http:COMPRESSION_NEVER, contentTypes:["text/plain","application/xml"]}}
service<http:Service> neverCompressWithContentType bind mockEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    test3 (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setTextPayload("Hello World!!!");
        _ = conn -> respond(res);
    }
}

@http:ServiceConfig {basePath : "/userOverridenValue", compression: {enable: http:COMPRESSION_NEVER}}
service<http:Service> userOverridenValue bind mockEP {
    @http:ResourceConfig {
            methods:["GET"],
            path:"/"
    }
    test3 (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setTextPayload("Hello World!!!");
        res.setHeader("content-encoding", "deflate");
        _ = conn -> respond(res);
    }
}

@http:ServiceConfig {compression: {contentTypes:["text/plain"]}}
service<http:Service> autoCompressWithInCompatibleContentType bind mockEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    test1 (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setJsonPayload({ test: "testValue" }, contentType = "application/json;charset=\"ISO_8859-1:1987\"");
        _ = conn -> respond(res);
    }
}

@http:ServiceConfig {compression: {enable: http:COMPRESSION_ALWAYS, contentTypes:[]}}
service<http:Service> alwaysCompressWithEmptyContentType bind mockEP {
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
