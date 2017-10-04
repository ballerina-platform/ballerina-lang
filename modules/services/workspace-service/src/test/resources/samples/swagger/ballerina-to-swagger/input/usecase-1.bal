import ballerina.net.http;
import ballerina.net.http.response;

service<http> Service1 {

    @http:resourceConfig {
        methods: ["GET"]
    }
    resource Resource1 (http:Request req, http:Response res) {
        response:send(res);
    }
}
