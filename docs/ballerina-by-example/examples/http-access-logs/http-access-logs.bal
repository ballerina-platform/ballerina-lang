import ballerina.net.http;
@http:configuration {
    basePath:"/hello",
    port:9095
}
service<http> helloService {
    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource aGetRequest (http:Connection conn, http:Request requ) {
        http:Response res = {};
        res.setStringPayload("Successful");
        _ = conn.respond(res);
    }
}