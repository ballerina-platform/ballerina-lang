import ballerina.net.http;

@http:configuration {basePath:"/identifierLiteral"}
service<http> |sample service| {

    @http:resourceConfig {
        methods:["GET"],
        path:"/resource"
    }
    resource |sample resource| (http:Connection conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"key":"keyVal", "value":"valueOfTheString"};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/resource2"
    }
    resource |sample resource2| (http:Connection conn, http:Request req) {
        http:Response res = {};
        string |a a| = "hello";
        res.setStringPayload(|a a|);
        _ = conn.respond(res);
    }
}

