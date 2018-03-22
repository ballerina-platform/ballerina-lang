import ballerina/net.http;
import ballerina/net.http.mock;

endpoint mock:NonListeningService helloEP {
    port:9090
};

@http:ServiceConfig {
    basePath:"/hello",
    endpoints:[helloEP]
}
service<http:Service> helloWorldResourceConfig {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    @http:ResourceConfig {
        methods:["POST"]
    }
    sayHello (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("Hello World !!!");
        _ = conn -> respond(res);
    }
}
