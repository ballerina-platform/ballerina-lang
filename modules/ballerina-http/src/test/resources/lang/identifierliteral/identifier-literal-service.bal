import ballerina.net.http;
import ballerina.net.http.response;

@http:configuration {basePath:"/identifierLiteral"}
service<http> |sample service| {

    @http:resourceConfig {
        methods:["GET"],
        path:"/resource"
    }
    resource |sample resource| (http:Request req, http:Response res) {
        json responseJson = {"key":"keyVal", "value":"valueOfTheString"};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }
}

