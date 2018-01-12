import ballerina.net.http;

@http:configuration {basePath:"/identifierLiteral"}
service<http> |sample service| {

    @http:resourceConfig {
        methods:["GET"],
        path:"/resource"
    }
    resource |sample resource| (http:Request req, http:Response res) {
        json responseJson = {"key":"keyVal", "value":"valueOfTheString"};
        res.setJsonPayload(responseJson);
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/resource2"
    }
    resource |sample resource2| (http:Request req, http:Response res) {
        string |a a| = "hello";
        res.setStringPayload(|a a|);
        _ = res.send();
    }
}

