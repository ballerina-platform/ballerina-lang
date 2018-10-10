import ballerina/http;

endpoint http:NonListener testEP {
    port: 9090
};

service<http:Service> echo66 bind testEP {
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/test1",
        consumes: ["application/xml"]
    }
    echo1(endpoint conn, http:Request req) {
        _ = conn->respond({ "msg": "wso2" });
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/test2",
        produces: ["text/xml", "application/xml "]
    }
    echo2(endpoint conn, http:Request req) {
        _ = conn->respond({ "msg": "wso22" });
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/test3",
        consumes: ["application/xhtml+xml", "text/plain", "text/json"],
        produces: ["text/css", "application/json"]
    }
    echo3(endpoint conn, http:Request req) {
        _ = conn->respond({ "msg": "wso222" });
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/test4",
        consumes: ["appliCation/XML"],
        produces: ["Application/JsON"]
    }
    echo4(endpoint conn, http:Request req) {
        _ = conn->respond({ "msg": "wso222" });
    }
}

service<http:Service> echo67 bind testEP {
    echo1(endpoint conn, http:Request req) {
        _ = conn->respond({ "echo33": "echo1" });
    }
}
