import ballerina/http;
import ballerina/http;

endpoint http:NonListeningServiceEndpoint helloEP {
    port:9090
};

@http:ServiceConfig {basePath:"/hello"}
@http:ServiceConfig {transactionInfectable: false}
service<http:Service> helloWorldServiceConfig bind helloEP{

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    sayHello (endpoint conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("Hello World!!!");
        _ = conn -> respond(res);
    }
}
