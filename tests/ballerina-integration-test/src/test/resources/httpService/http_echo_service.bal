import ballerina/http;

endpoint http:Listener echoEP1 {
    port:9094
};

endpoint http:Listener echoEP2 {
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
    echo (endpoint caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("hello world");
        _ = caller -> respond(res);
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
    echoAbc (endpoint caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("hello world");
        _ = caller -> respond(res);
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
    echoDummy (endpoint caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("hello world");
        _ = caller -> respond(res);
    }

    @http:ResourceConfig {
        methods:["OPTIONS"],
        path:"/getOptions"
    }
    echoOptions (endpoint caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("hello Options");
        _ = caller -> respond(res);
    }
}
