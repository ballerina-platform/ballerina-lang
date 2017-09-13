import ballerina.lang.messages;
import ballerina.net.http;

@http:configuration {basePath:"/identifierLiteral"}
service<http> |sample service| {

    @http:resourceConfig {
        methods:["GET"],
        path:"/resource"
    }
    resource |sample resource| (message m) {
        message response = {};

        json responseJson = {"key":"keyVal", "value":"valueOfTheString"};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }
}

