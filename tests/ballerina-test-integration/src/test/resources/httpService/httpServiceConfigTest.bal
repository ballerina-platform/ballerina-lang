import ballerina.net.http;

@http:configuration {
    basePath:"/compressEnabled",
    compressionEnabled:true
}
service<http> compressEnabled {
    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource echo (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        res.setStringPayload("hello world");
        _ = conn.respond(res);
    }
}


@http:configuration {
    basePath:"/compressDisabled",
    compressionEnabled:false
}
service<http> compressDisabled {
    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource echo (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        res.setStringPayload("hello world");
        _ = conn.respond(res);
    }
}