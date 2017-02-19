package mock;

import ballerina.lang.messages as message;

@http:BasePath ("/mock")
service mockService {

    @http:GET
    resource mockResource(message m) {
        message resp = {};
        message:setStringPayload(resp, "You invoked mockService!");
        reply resp;
    }
}
