import ballerina/net.http;
import ballerina/net.http.mock;

endpoint<mock:NonListeningService> mockEP {
    port:9090
}

@http:serviceConfig {endpoints:[mockEP]}
service<http:Service> hello {

    @http:resourceConfig {
        path:"/10"
    }
    resource echo10 (http:ServerConnector conn, http:Request req) {
        http:Response resp = {};
        _ = conn -> respond(null);
    }

    @http:resourceConfig {
        path:"/11"
    }
    resource echo11 (http:ServerConnector conn, http:Request req) {
        http:Response resp = {};
        http:ServerConnector connn = new http:ServerConnector();
        _ = connn -> respond(resp);
    }

    @http:resourceConfig {
        path:"/20"
    }
    resource echo20 (http:ServerConnector conn, http:Request req) {
        _ = conn -> forward(null);
    }

    @http:resourceConfig {
        path:"/21"
    }
    resource echo21 (http:ServerConnector conn, http:Request req) {
        http:Response resp = {};
        http:ServerConnector connn = new http:ServerConnector();
        _ = connn -> forward(resp);
    }

}
