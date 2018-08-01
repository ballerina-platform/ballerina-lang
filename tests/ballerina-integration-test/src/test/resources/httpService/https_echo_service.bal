import ballerina/http;

endpoint http:Listener echoDummyEP {
    port:9109
};

endpoint http:Listener echoHttpEP {
    port: 9110
};

endpoint http:Listener echoEP2 {
    port:9111,
    secureSocket: {
        keyStore: {
            path:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password:"ballerina"
        }
    }
};

@http:ServiceConfig {
    basePath:"/echo"
}

service<http:Service> echo2 bind echoEP2 {
    @http:ResourceConfig {
        methods:["POST"],
        path:"/"
    }
    echo2 (endpoint caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("hello world");
        _ = caller -> respond(res);
    }
}

@http:ServiceConfig  {
    basePath:"/echoOne"
}
service<http:Service> echoOne1 bind echoEP2, echoHttpEP {
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
service<http:Service> echoDummy1 bind echoDummyEP {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/"
    }
    echoDummy1 (endpoint caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("hello world");
        _ = caller -> respond(res);
    }
}
