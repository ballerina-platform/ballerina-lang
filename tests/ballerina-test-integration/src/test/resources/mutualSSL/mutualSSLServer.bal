import ballerina.io;
import ballerina.net.http;

endpoint<http:Service> echo {
    port:9095,
    ssl : {
            keyStoreFile:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
            keyStorePassword:"ballerina",
            certPassword:"ballerina",
            sslVerifyClient:"require",
            trustStoreFile:"${ballerina.home}/bre/security/ballerinaTruststore.p12",
            trustStorePassword:"ballerina",
            ciphers:"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
            sslEnabledProtocols:"TLSv1.2,TLSv1.1"
    }
}

@http:serviceConfig {
    endpoints:[echo],
    basePath:"/echo"
}
service<http: Service > helloWorld {

     @http:resourceConfig {
         methods:["GET"],
         path:"/"
     }
     resource sayHello (http:ServerConnector conn, http:Request req) {
         http:Response res = {};
         res. setStringPayload("hello world");
         _ = conn -> respond( res);
         io:println("successful");
     }
}

endpoint<http:Service> echoDummy {
    port:9090
}

@http:serviceConfig {
    endpoints:[echoDummy],
    basePath:"/echoDummy"
}
service<http:Service > echoDummyService {

     @http:resourceConfig {
         methods:["POST"],
         path:"/"
     }
     resource sayHello (http:ServerConnector conn, http:Request req) {
         http:Response res = {};
         res.setStringPayload("hello world");
         _ = conn -> respond(res);
     }
}