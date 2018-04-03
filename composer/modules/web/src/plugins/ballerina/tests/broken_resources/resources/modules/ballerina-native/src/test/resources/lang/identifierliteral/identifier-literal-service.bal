import ballerina/lang.messages;
import ballerina/http;

@http:BasePath {value:"/identifierLiteral"}
service |sample service| {

    @http:GET{}
    @http:Path {value:"/resource"}
    resource |sample resource| (message m) {
        message response = {};

        json responseJson = {"key":"keyVal", "value":"valueOfTheString"};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }
}

