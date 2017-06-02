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
    resource echo2 (message m, @http:PathParam {value:"abc"} string abc, @http:PathParam {value:"xyz"} string xyz) {
        message response = {};
        json responseJson = {"first":abc, "second":xyz};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:GET{}
    @http:Path {value:"/echo2/{abc}+{xyz}"}
    resource echo3 (message m, @http:PathParam {value:"abc"} string abc, @http:PathParam {value:"xyz"} string xyz) {
        message response = {};
        json responseJson = {"first":xyz, "second":abc};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:GET{}
    @http:Path {value:"/echo2/{abc}"}
    resource echo4 (message m, @http:PathParam {value:"abc"} string abc) {
        message response = {};
        json responseJson = {"echo3":abc};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:GET{}
    @http:Path {value:"/echo2/{abc}+{xyz}/bar"}
    resource echo5 (message m, @http:PathParam {value:"abc"} string abc, @http:PathParam {value:"xyz"} string xyz) {
        message response = {};
        json responseJson = {"first":abc, "second":xyz, "echo4": "echo4"};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:GET{}
    @http:Path {value:"/echo2/{abc}+{xyz}/{bar}"}
    resource echo6 (message m, @http:PathParam {value:"abc"} string abc,
                    @http:PathParam {value:"xyz"} string xyz, @http:PathParam {value:"bar"} string bar) {
        message response = {};
        json responseJson = {"first":abc, "second":xyz, "echo4": bar};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:GET{}
    @http:Path {value:"/echo2/*"}
    resource echo7 (message m) {
        message response = {};
        json responseJson = {"echo5":"any"};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:POST{}
    @http:Path {value:"/echo2/{abc}+{xyz}/bar"}
    resource echo8 (message m, @http:PathParam {value:"abc"} string abc1, @http:PathParam {value:"xyz"} string xyz2) {
        message response = {};
        json responseJson = {"first":abc1, "second":xyz2, "echo8": "echo8"};
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

