import ballerina/http;

listener http:MockListener testEP = new(9090);

service echo66 on testEP {
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/test1",
        consumes: ["application/xml"]
    }
    resource function echo1(http:Caller caller, http:Request req) {
        checkpanic caller->respond({ "msg": "wso2" });
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/test2",
        produces: ["text/xml", "application/xml "]
    }
    resource function echo2(http:Caller caller, http:Request req) {
        checkpanic caller->respond({ "msg": "wso22" });
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/test3",
        consumes: ["application/xhtml+xml", "text/plain", "text/json"],
        produces: ["text/css", "application/json"]
    }
    resource function echo3(http:Caller caller, http:Request req) {
        checkpanic caller->respond({ "msg": "wso222" });
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/test4",
        consumes: ["appliCation/XML"],
        produces: ["Application/JsON"]
    }
    resource function echo4(http:Caller caller, http:Request req) {
        checkpanic caller->respond({ "msg": "wso222" });
    }
}

service echo67 on testEP {
    resource function echo1(http:Caller caller, http:Request req) {
        checkpanic caller->respond({ "echo33": "echo1" });
    }
}
