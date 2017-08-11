import ballerina.net.http;
import ballerina.lang.messages;

@http:configuration {basePath:"/echo66"}
service<http> echo66 {
    @http:POST{}
    @http:Path{value:"/test1"}
    @http:Consumes{value:["application/xml"]}
    resource echo1 (message m) {
    	message response = {};
        json responseJson = {"msg":"wso2"};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:GET{}
    @http:Path{value:"/test2"}
    @http:Produces{value:["text/xml","application/xml "]}
    resource echo2 (message m) {
        message response = {};
        json responseJson = {"msg":"wso22"};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:POST{}
    @http:Path{value:"/test3"}
    @http:Consumes{value:["application/xhtml+xml","text/plain","text/json"]}
    @http:Produces{value:["text/css","application/json"]}
    resource echo3 (message m) {
        message response = {};
        json responseJson = {"msg":"wso222"};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }
}

@http:configuration {basePath:"/echo67"}
service<http> echo67 {
    resource echo1 (message m, string foo) {
        message response = {};
        json responseJson = {"third":foo, "echo33": "echo1"};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }
}