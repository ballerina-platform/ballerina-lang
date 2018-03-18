import ballerina.net.http;

endpoint http:ServiceEndpoint echoEP1 {
    port:9094
};

endpoint http:ServiceEndpoint echoEP2 {
    port:9090
};

@http:serviceConfig {
    basePath:"/echo"
}
service<http:Service> echo bind echoEP1 {
    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    echo (endpoint outboundEP, http:Request req) {
        http:Response res = {};
        res.setStringPayload("hello world");
        _ = outboundEP -> respond(res);
    }
}

@http:serviceConfig {
    basePath:"/echoOne"
}
service<http:Service> echoOne bind echoEP1 {
    @http:resourceConfig {
        methods:["POST"],
        path:"/abc"
    }
    echoAbc (endpoint outboundEP, http:Request req) {
        http:Response res = {};
        res.setStringPayload("hello world");
        _ = outboundEP -> respond(res);
    }
}

@http:serviceConfig {
    basePath:"/echoDummy"
}
service<http:Service> echoDummy bind echoEP2 {

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    echoDummy (endpoint outboundEP, http:Request req) {
        http:Response res = {};
        res.setStringPayload("hello world");
        _ = outboundEP -> respond(res);
    }

    @http:resourceConfig {
        methods:["OPTIONS"],
        path:"/getOptions"
    }
    echoOptions (endpoint outboundEP, http:Request req) {
        http:Response res = {};
        res.setStringPayload("hello Options");
        _ = outboundEP -> respond(res);
    }
}
