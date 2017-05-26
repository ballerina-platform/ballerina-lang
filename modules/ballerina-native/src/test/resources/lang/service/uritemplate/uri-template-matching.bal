import ballerina.net.http;
import ballerina.lang.messages;

@http:BasePath {value:"/hello"}
service echo4 {

    @http:GET{}
    @http:Path {value:"/echo2"}
    resource echo1 (message m) {
        message response = {};
        json responseJson = {"echo5":"echo5"};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:GET{}
    @http:Path {value:"/echo2/{abc}-{xyz}"}
    resource echo1 (message m, @http:PathParam {value:"abc"} string abc, @http:PathParam {value:"xyz"} string xyz) {
        message response = {};
        json responseJson = {"first":abc, "second":xyz};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }
}

@http:BasePath {value:"/hello/world"}
service echo {

    @http:GET{}
    @http:Path {value:"/echo2"}
    resource echo1 (message m) {
        message response = {};
        json responseJson = {"echo1":"echo1"};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:GET{}
    @http:Path {value:"/echo2/*"}
    resource echo2 (message m) {
        message response = {};
        json responseJson = {"echo2":"echo2"};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:GET{}
    @http:Path {value:"/echo2/foo/bar"}
    resource echo3 (message m) {
        message response = {};
        json responseJson = {"echo3":"echo3"};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }
}

