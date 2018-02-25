import ballerina.io;
import ballerina.net.http;

@http:configuration {
    basePath:"/echo",
    httpsPort:9095,
    keyStoreFile:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
    keyStorePassword:"ballerina",
    certPassword:"ballerina",
    sslVerifyClient:"require",
    trustStoreFile:"${ballerina.home}/bre/security/ballerinaTruststore.p12",
    trustStorePassword:"ballerina",
    ciphers:"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
    sslEnabledProtocols:"TLSv1.2,TLSv1.1"
}
service<http> echo {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource echo (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        res.setStringPayload("hello world");
        _ = conn.respond(res);
        io:println("successful");
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