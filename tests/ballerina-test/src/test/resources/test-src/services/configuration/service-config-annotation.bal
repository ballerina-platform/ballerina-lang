import ballerina/net.http;
import ballerina/net.http.mock;

endpoint mock:NonListeningService helloEP {
    port:9090
};

@http:ServiceConfig {basePath:"/hello"}
@http:ServiceConfig {endpoints:[helloEP]}
service<http:Service> helloWorldServiceConfig {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    sayHello (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("Hello World!!!");
        _ = conn -> respond(res);
    }
}
