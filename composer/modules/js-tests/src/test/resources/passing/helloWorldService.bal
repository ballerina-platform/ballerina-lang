import ballerina.net.http;
import ballerina.net.http.response;

@http:configuration {basePath:"/hello"}
service<http> helloWorld {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource sayHello (http:Request req, http:Response res) {
        response:setStringPayload(res, "Hello, World!");
        response:send(res);
    }
    
}
