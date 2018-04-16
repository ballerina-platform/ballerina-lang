import ballerina/http;
@Description {value:"The Ballerina server connector can be used to connect to an https client. To verify the server authenticity when establishing the connection, provide a `keyStore filePath` and `keyStore password".}
endpoint http:Listener helloWorldEP {
    port:9095,
    secureSocket: {
        keyStore: {
            filePath: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
};

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
