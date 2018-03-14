import ballerina.net.http;


endpoint<http:Service> helloWorldEP {
    port:9095,
    ssl : {
            keyStoreFile:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
            keyStorePassword:"ballerina",
            certPassword:"ballerina"
     }
}

@Description {value:"Ballerina server connector can be used to connect to a https client. If client needs to verify server authenticity when establishing the connection, server needs to provide keyStoreFile, keyStorePassword and certificate password as given here."}
@http:serviceConfig {
    endpoints:[helloWorldEP],
    basePath:"/hello"
}
service<http:Service> helloWorld {
    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }

    resource sayHello (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("Successful");
        _ = conn -> respond(res);
    }
}
