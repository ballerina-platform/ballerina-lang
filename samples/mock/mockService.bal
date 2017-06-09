package mock;

import ballerina.lang.messages;
import ballerina.net.http;

@http:BasePath {value: "/mock"}
service mockService {

    @http:GET{}
    @http:Path{ value: "/"}
    resource mockResource(message m) {
        message resp = {};
        messages:setStringPayload(resp, "You invoked mockService!");
        reply resp;
    }
}
