import ballerina/io;
import ballerina/http;

endpoint http:Listener echo {
    port:9095,
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        },
        trustStore: {
            path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        },
        protocol: {
            name: "TLSv1.2",
            versions: ["TLSv1.2","TLSv1.1"]
        },
        ciphers:["TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"],
        sslVerifyClient:"require"
    }
};

@http:ServiceConfig {
     endpoints:[echo],
     basePath:"/echo"
}

service<http:Service> helloWorld bind echo {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    sayHello (endpoint conn, http:Request req) {
        http:Response res = new;
        res. setStringPayload("hello world");
        _ = conn -> respond( res);
        io:println("successful");
    }
}

endpoint http:Listener echoDummy {
    port:9090
};

@http:ServiceConfig {
      endpoints:[echoDummy],
      basePath:"/echoDummy"
}
service<http:Service> echoDummyService bind echoDummy {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/"
    }
    sayHello (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setStringPayload("hello world");
        _ = conn -> respond(res);
    }
}