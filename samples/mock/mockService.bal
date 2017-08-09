package mock;

import ballerina.lang.messages;
import ballerina.net.http;

@http:configuration {basePath:"/mock"}
service<http> mockService {

    @http:GET{}
    @http:Path{ value: "/"}
    resource mockResource(message m) {
        message resp = {};
        messages:setStringPayload(resp, "You invoked mockService!");
        reply resp;
    }
}
