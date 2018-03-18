import ballerina.net.http;

endpoint http:ServiceEndpoint echoDummyEP {
    port:9090
};

endpoint http:ServiceEndpoint echoHttpEP {
    port: 9094
};

endpoint http:ServiceEndpoint echoEP {
    port:9095,
    ssl:{
        keyStoreFile:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
        keyStorePassword:"ballerina",
        certPassword:"ballerina"
    }
};

@http:serviceConfig {
    basePath:"/echo"
}

service<http:Service> echo bind echoEP {
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

@http:serviceConfig  {
    basePath:"/echoOne"
}
service<http:Service> echoOne bind echoEP, echoHttpEP {
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
service<http:Service> echoDummy bind echoDummyEP {

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    echoDummy (endpoint outboundEP, http:Request req) {
        http:Response res = {};
        res.setStringPayload("hello world");
        _ = outboundEP -> respond(res);
    }
}
