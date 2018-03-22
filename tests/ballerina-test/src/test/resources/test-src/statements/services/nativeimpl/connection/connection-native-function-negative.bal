import ballerina/net.http;
import ballerina/net.http.mock;

endpoint mock:NonListeningServiceEndpoint mockEP {
    port:9090
};

service<http:Service> hello bind mockEP {

    @http:ResourceConfig {
        path:"/10"
    }
    echo10 (endpoint conn, http:Request req) {
        http:Response resp = {};
        _ = conn -> respond(null);
    }

    @http:ResourceConfig {
        path:"/11"
    }
    echo11 (endpoint conn, http:Request req) {
        http:Response resp = {};
        http:ServerConnector connn = new http:ServerConnector();
        _ = connn -> respond(resp);
    }

    @http:ResourceConfig {
        path:"/20"
    }
    echo20 (endpoint conn, http:Request req) {
        _ = conn -> forward(null);
    }

    @http:ResourceConfig {
        path:"/21"
    }
    echo21 (endpoint conn, http:Request req) {
        http:Response resp = {};
        http:ServerConnector connn = new http:ServerConnector();
        _ = connn -> forward(resp);
    }

}
