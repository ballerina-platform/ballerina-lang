import ballerina.net.http;
import ballerina.lang.messages;

@http:BasePath {value:"/hello/world"}
service echo {

    @http:GET{}
    @http:Path {value:"/echo2?regid={abc}"}
    resource echo1 (message m) {
        message response = {};
        json responseJson = `{"echo1":"echo1"}`;
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:GET{}
    @http:Path {value:"/echo2/*"}
    resource echo2 (message m) {
        message response = {};
        json responseJson = `{"echo2":"echo2"}`;
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:GET{}
    @http:Path {value:"/echo2/foo/bar"}
    resource echo3 (message m) {
        message response = {};
        json responseJson = `{"echo3":"echo3"}`;
        messages:setJsonPayload(response, responseJson);
        reply response;
    }
}

