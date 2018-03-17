import ballerina.net.http;
import ballerina.net.http.mock;

endpoint<mock:NonListeningService> mockEP {
    port:9090
}

@http:serviceConfig {endpoints:[mockEP], compression: http:Compression.AUTO}
service<http:Service> autoCompress {
    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource test1 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("Hello World!!!");
        _ = conn -> respond(res);
    }
}

@http:serviceConfig {endpoints:[mockEP], compression: http:Compression.ALWAYS}
service<http:Service> alwaysCompress {
     @http:resourceConfig {
        methods:["GET"],
        path:"/"
        }
    resource test2 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("Hello World!!!");
        _ = conn -> respond(res);
    }
}

@http:serviceConfig {endpoints:[mockEP], compression: http:Compression.NEVER}
service<http:Service> neverCompress {
    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource test3 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("Hello World!!!");
        _ = conn -> respond(res);
    }
}

@http:serviceConfig {endpoints:[mockEP], compression: http:Compression.NEVER}
service<http:Service> userOverridenValue {
    @http:resourceConfig {
            methods:["GET"],
            path:"/"
    }
    resource test3 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("Hello World!!!");
        res.setHeader("content-encoding", "deflate");
        _ = conn -> respond(res);
    }
}