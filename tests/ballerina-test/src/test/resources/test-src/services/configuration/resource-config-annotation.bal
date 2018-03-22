import ballerina/net.http;
import ballerina/net.http.mock;

endpoint mock:NonListeningService helloEP {
    port:9090
};

@http:serviceConfig {
    basePath:"/hello",
    endpoints:[helloEP]
}
service<http:Service> helloWorldResourceConfig {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    @http:resourceConfig {
        methods:["POST"]
    }
    sayHello (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("Hello World !!!");
        _ = conn -> respond(res);
    }
}
