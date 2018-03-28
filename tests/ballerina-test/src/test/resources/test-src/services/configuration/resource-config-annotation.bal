import ballerina/net.http;
import ballerina/net.http.mock;

endpoint mock:NonListeningServiceEndpoint helloEP {
    port:9090
};

@http:ServiceConfig {
    basePath:"/hello"
}
service<http:Service> helloWorldResourceConfig bind helloEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    @http:ResourceConfig {
        methods:["POST"]
    }
    sayHello (endpoint conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("Hello World !!!");
        _ = conn -> respond(res);
    }
}
