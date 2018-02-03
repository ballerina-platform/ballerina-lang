import ballerina.net.http;

@http:configuration {
    basePath:"/echo",
    port:9094
}
service<http> echo {
    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource echo (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        res.setStringPayload("hello world");
        _ = conn.respond(res);
    }
}

@http:configuration {
    basePath:"/echoOne",
    port:9094
}
service<http> echoOne {
    @http:resourceConfig {
        methods:["POST"],
        path:"/abc"
    }
    resource echoAbc (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        res.setStringPayload("hello world");
        _ = conn.respond(res);
    }
}

@http:configuration {
    basePath:"/echoDummy"
}
service<http> echoDummy {

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource echoDummy (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        res.setStringPayload("hello world");
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["OPTIONS"],
        path:"/getOptions"
    }
    resource echoOptions (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        res.setStringPayload("hello Options");
        _ = conn.respond(res);
    }
}
