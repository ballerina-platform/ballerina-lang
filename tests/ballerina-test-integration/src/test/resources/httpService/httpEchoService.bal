import ballerina.net.http;

endpoint<http:Service> echoEP1 {
    port:9094
}

endpoint<http:Service> echoEP2 {
    port:9090
}

@http:serviceConfig {
    basePath:"/echo",
    endpoints: [echoEP1]
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

@http:serviceConfig {
    basePath:"/echoOne",
    endpoints: [echoEP1]
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
    endpoints: [echoEP2]
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

    @http:resourceConfig {
        methods:["OPTIONS"],
        path:"/getOptions"
    }
    resource echoOptions (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("hello Options");
        _ = conn -> respond(res);
    }
}
