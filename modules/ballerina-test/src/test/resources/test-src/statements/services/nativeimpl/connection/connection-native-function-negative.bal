import ballerina.net.http;

@http:configuration{basePath:"/hello"}
service<http> helloServer {

    @http:resourceConfig {
        path:"/10"
    }
    resource echo10 (http:Connection conn, http:InRequest req) {
        http:OutResponse resp = {};
        _ = conn.respond(null);
    }

    @http:resourceConfig {
        path:"/11"
    }
    resource echo11 (http:Connection conn, http:InRequest req) {
        http:OutResponse resp = {};
        http:Connection connn = {};
        _ = connn.respond(resp);
    }

    @http:resourceConfig {
        path:"/12"
    }
    resource echo12 (http:Connection conn, http:InRequest req) {
        _ = conn.respond({});
    }

    @http:resourceConfig {
        path:"/20"
    }
    resource echo20 (http:Connection conn, http:InRequest req) {
        _ = conn.forward(null);
    }

    @http:resourceConfig {
        path:"/21"
    }
    resource echo21 (http:Connection conn, http:InRequest req) {
        http:InResponse resp = {};
        http:Connection connn = {};
        _ = connn.forward(resp);
    }

    @http:resourceConfig {
        path:"/22"
    }
    resource echo22 (http:Connection conn, http:InRequest req) {
        _ = conn.forward({});
    }
}
