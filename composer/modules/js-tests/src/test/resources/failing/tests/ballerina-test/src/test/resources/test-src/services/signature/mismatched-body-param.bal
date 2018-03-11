import ballerina.net.http;

@http:configuration {basePath:"/signature"}
service<http> echo {
    @http:resourceConfig {
        methods:["POST"],
        body:"person"
    }
    resource echo1 (http:Connection conn, http:InRequest req, string key, json ballerina) {
        http:OutResponse res = {};
        _ = conn.respond(res);
    }
}
