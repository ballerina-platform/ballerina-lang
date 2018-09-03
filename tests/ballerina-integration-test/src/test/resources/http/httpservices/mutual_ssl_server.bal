import ballerina/io;
import ballerina/http;

endpoint http:Listener echo15 {
    port:9116,
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
        sslVerifyClient:"require",
        certValidation: {
            enable: false
        },
        ocspStapling: {
            enable: false
        }
    }
};

@http:ServiceConfig {
     basePath:"/echo"
}

service<http:Service> helloWorld15 bind echo15 {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    sayHello (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setTextPayload("hello world");
        _ = conn -> respond( res);
        io:println("successful");
    }
}

endpoint http:Listener echoDummy15 {
    port:9117
};

@http:ServiceConfig {
    basePath:"/echoDummy"
}
service<http:Service> echoDummyService15 bind echoDummy15 {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/"
    }
    sayHello (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setTextPayload("hello world");
        _ = conn -> respond(res);
    }
}