import ballerina/http;
import ballerina/lang.messages;

@http:configuration {basePath:"/echo66"}
service<http> echo66 {
    @http:resourceConfig {
        methods:["POST"],
        path:"/test1",
        consumes:["application/xml"]
    }
    resource echo1 (message m) {
    	message response = {};
        json responseJson = {"msg":"wso2"};
        messages:setJsonPayload(response, responseJson);
        response:send(response);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/test2",
        produces:["text/xml","application/xml "]
    }
    resource echo2 (message m) {
        message response = {};
        json responseJson = {"msg":"wso22"};
        messages:setJsonPayload(response, responseJson);
        response:send(response);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/test3",
        consumes:["application/xhtml+xml","text/plain","text/json"],
        produces:["text/css","application/json"]
    }
    resource echo3 (message m) {
        message response = {};
        json responseJson = {"msg":"wso222"};
        messages:setJsonPayload(response, responseJson);
        response:send(response);
    }
}

@http:configuration {basePath:"/echo67"}
service<http> echo67 {
    resource echo1 (message m, string foo) {
        message response = {};
        json responseJson = {"third":foo, "echo33": "echo1"};
        messages:setJsonPayload(response, responseJson);
        response:send(response);
    }
}