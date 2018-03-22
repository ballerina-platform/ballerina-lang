import ballerina/net.http;
import ballerina/net.http.mock;

endpoint mock:NonListeningService helloEP {
    port:9090
};

@http:serviceConfig {basePath:"/hello"}
@http:serviceConfig {endpoints:[helloEP]}
service<http:Service> helloWorldServiceConfig {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    sayHello (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("Hello World!!!");
        _ = conn -> respond(res);
    }
}
