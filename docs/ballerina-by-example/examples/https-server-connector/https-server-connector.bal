import ballerina/http;

endpoint http:Listener helloWorldEP {
    port:9095,
    secureSocket: {
        keyStore: {
            filePath: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
};

@Description {value:"Ballerina server connector can be used to connect to a https client. If client needs to verify server authenticity when establishing the connection, server needs to provide keyStoreFile, keyStorePassword and certificate password as given here."}
@http:ServiceConfig {
      endpoints:[helloWorldEP],
      basePath:"/hello"
}
service<http:Service> helloWorld bind helloWorldEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }

    sayHello (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setStringPayload("Successful");
        _ = conn -> respond(res);
    }
}
