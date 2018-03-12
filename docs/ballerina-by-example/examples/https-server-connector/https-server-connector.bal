import ballerina.net.http;

@Description {value:"Ballerina server connector can be used to connect to a https client. If client needs to verify server authenticity when establishing the connection, server needs to provide keyStoreFile, keyStorePassword and certificate password as given here."}
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

    resource sayHello (http:Connection conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("Successful");
        _ = conn.respond(res);
    }
}
