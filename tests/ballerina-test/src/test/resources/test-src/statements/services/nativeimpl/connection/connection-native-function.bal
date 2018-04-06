import ballerina/http;

endpoint http:NonListeningServiceEndpoint mockEP {
    port:9090
};

service<http:Service> hello bind mockEP {
    @http:ResourceConfig {
        path:"/redirect",
        methods:["GET"]
    }
    redirect (endpoint conn, http:Request req) {
        http:Response res = new;
        _ = conn -> redirect(res, http:REDIRECT_MOVED_PERMANENTLY_301, ["location1"]);
    }
}
