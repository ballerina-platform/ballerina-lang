import ballerina/http;

endpoint http:Listener echoDummyEP {
    port:9090
};

endpoint http:Listener echoHttpEP {
    port: 9094
};

endpoint http:Listener echoEP {
    port:9095,
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

service<http:Service> echo bind echoEP {
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

@http:ServiceConfig  {
    basePath:"/echoOne"
}
service<http:Service> echoOne bind echoEP, echoHttpEP {
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
service<http:Service> echoDummy bind echoDummyEP {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/"
    }
    echoDummy (endpoint outboundEP, http:Request req) {
        http:Response res = new;
        res.setStringPayload("hello world");
        _ = outboundEP -> respond(res);
    }
}
