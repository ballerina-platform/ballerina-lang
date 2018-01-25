import ballerina.net.http;

@Description {value:"Services can be configured to listen to and serve HTTPS requests. To do so, a keystore (containing a certificate and a key) has to be provided."}
@http:configuration {
    basePath:"/hello",
    httpsPort:9095,
    keyStoreFile:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
    keyStorePassword:"ballerina",
    certPassword:"ballerina"
}

service<http> helloWorld {
    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }

    resource sayHello (http:Request req, http:Response res) {
        res.setStringPayload("Hello World!");
        _ = res.send();
    }
}
