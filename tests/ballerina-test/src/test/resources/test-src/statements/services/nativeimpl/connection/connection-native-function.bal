import ballerina/net.http;
import ballerina/net.http.mock;

endpoint<mock:NonListeningService> mockEP {
    port:9090
}
@http:serviceConfig {endpoints:[mockEP]}
service<http:Service> hello {
    @http:resourceConfig {
        path:"/redirect",
        methods:["GET"]
    }
    resource redirect(http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        _ = conn -> redirect(res, http:RedirectCode.MOVED_PERMANENTLY_301, ["location1"]);
    }
}
