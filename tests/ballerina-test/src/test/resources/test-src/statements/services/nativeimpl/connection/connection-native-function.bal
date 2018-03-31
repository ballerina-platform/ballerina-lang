import ballerina/net.http;
import ballerina/net.http.mock;

endpoint mock:NonListeningServiceEndpoint mockEP {
    port:9090
};

service<http:Service> hello bind mockEP {
    @http:ResourceConfig {
        path:"/redirect",
        methods:["GET"]
    }
    redirect (endpoint conn, http:Request req) {
        http:Response res = {};
        _ = conn -> redirect(res, http:RedirectCode.MOVED_PERMANENTLY_301, ["location1"]);
    }
}
