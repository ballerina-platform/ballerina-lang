import ballerina.net.http;

endpoint<http:Service> echoDummyEP {
    port:9090
}

endpoint<http:Service> echoHttpEP {
    port: 9094
}

endpoint<http:Service> echoEP {
    port:9095,
    ssl:{
        keyStoreFile:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
        keyStorePassword:"ballerina",
        certPassword:"ballerina"
    }
}

@http:serviceConfig {
    basePath:"/echo",
    endpoints: [echoEP]
}

service<http:Service> echo {
    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource echo (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("hello world");
        _ = conn -> respond(res);
    }
}

@http:serviceConfig  {
    basePath:"/echoOne",
    endpoints: [echoHttpEP, echoEP]
}
service<http:Service> echoOne {
    @http:resourceConfig {
        methods:["POST"],
        path:"/abc"
    }
    resource echoAbc (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("hello world");
        _ = conn -> respond(res);
    }
}

@http:serviceConfig {
    basePath:"/echoDummy",
    endpoints: [echoDummyEP]
}
service<http:Service> echoDummy {

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource echoDummy (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("hello world");
        _ = conn -> respond(res);
    }
}