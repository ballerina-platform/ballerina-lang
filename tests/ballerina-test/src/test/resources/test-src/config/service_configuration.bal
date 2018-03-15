import ballerina.net.http;
import ballerina.config;

endpoint<http:Service> backendEP {
    port: getBackendPort()
}

@http:serviceConfig {
    basePath: config:getAsString("hello.basePath"),
    endpoints:[backendEP]
}
service<http:Service> hello {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource sayHello (http:ServerConnector conn, http:Request request) {
        http:Response response = {};
        response.setStringPayload("Hello World!!!");
        _ = conn -> respond(response);
    }
}

function getBackendPort() (int) {
    var port, err = <int>config:getAsString("backendEP.port");
    return err == null ? port : 8000;
}
