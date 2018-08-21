import ballerina/http;

endpoint http:Listener echoEP3 {
    port:9099
};

endpoint http:Listener echoEP4 {
    port:9100
};

@http:ServiceConfig {
    basePath:"/echo"
}
service<http:Service> echo3 bind echoEP3 {
    @http:ResourceConfig {
        methods:["POST"],
        path:"/"
    }
    echo3 (endpoint caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("hello world");
        _ = caller -> respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/echoOne"
}
service<http:Service> echoOne2 bind echoEP3 {
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
service<http:Service> echoDummy2 bind echoEP4 {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/"
    }
    echoDummy2 (endpoint caller, http:Request req) {
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
