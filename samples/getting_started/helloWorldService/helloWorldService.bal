import ballerina.net.http;

@http:configuration {basePath:"/hello"}
service<http> helloWorld {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource sayHello (http:Connection con, http:Request req) {
        http:Response resp = {};
        resp.setStringPayload("Hello, World!");
        _ = con.respond(resp);
    }
}
