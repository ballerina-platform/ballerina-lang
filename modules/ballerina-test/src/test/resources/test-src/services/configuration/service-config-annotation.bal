import ballerina.net.http;

@http:configuration {basePath:"/hello"}
@http:configuration {port: 9090}
service<http> helloWorldServiceConfig {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource sayHello (http:Connection con, http:Request req) {
        http:Response res = {};
        res.setStringPayload("Hello World !!!");
        _ = con.respond(res);
    }
}
