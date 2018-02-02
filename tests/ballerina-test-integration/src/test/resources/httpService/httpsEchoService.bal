import ballerina.net.http;

@http:configuration {
    basePath:"/echo",
    httpsPort:9095,
    keyStoreFile:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
    keyStorePassword:"ballerina",
    certPassword:"ballerina"
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
    port:9094,
    httpsPort:9095,
    keyStoreFile:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
    keyStorePassword:"ballerina",
    certPassword:"ballerina"
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
}
