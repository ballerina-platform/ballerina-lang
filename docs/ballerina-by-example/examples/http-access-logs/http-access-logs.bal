import ballerina.net.http;

endpoint<http:Service> helloServiceEP {
    port:9095
}

@http:serviceConfig { basePath: "/hello", endpoints:[helloServiceEP] }
service<http:Service> helloService {
    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource aGetRequest (http:ServerConnector conn, http:Request requ) {
        http:Response res = {};
        res.setStringPayload("Successful");
        _ = conn -> respond(res);
    }
}
