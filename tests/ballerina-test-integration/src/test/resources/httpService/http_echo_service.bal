import ballerina/http;

endpoint http:ServiceEndpoint echoEP1 {
    port:9094
};

endpoint http:ServiceEndpoint echoEP2 {
    port:9090
};

@http:ServiceConfig {
    basePath:"/echo"
}
service<http:Service> echo bind echoEP1 {
    @http:ResourceConfig {
        methods:["POST"],
        path:"/"
    }
    echo (endpoint outboundEP, http:Request req) {
        http:Response res = new;
        res.setStringPayload("hello world");
        _ = outboundEP -> respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/echoOne"
}
service<http:Service> echoOne bind echoEP1 {
    @http:ResourceConfig {
        methods:["POST"],
        path:"/abc"
    }
    echoAbc (endpoint outboundEP, http:Request req) {
        http:Response res = new;
        res.setStringPayload("hello world");
        _ = outboundEP -> respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/echoDummy"
}
service<http:Service> echoDummy bind echoEP2 {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/"
    }
    echoDummy (endpoint outboundEP, http:Request req) {
        http:Response res = new;
        res.setStringPayload("hello world");
        _ = outboundEP -> respond(res);
    }

    @http:ResourceConfig {
        methods:["OPTIONS"],
        path:"/getOptions"
    }
    echoOptions (endpoint outboundEP, http:Request req) {
        http:Response res = new;
        res.setStringPayload("hello Options");
        _ = outboundEP -> respond(res);
    }
}
