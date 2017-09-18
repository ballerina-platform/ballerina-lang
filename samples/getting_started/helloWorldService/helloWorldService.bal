import ballerina.net.http;
import ballerina.net.http.response as res;

@http:configuration {basePath:"/hello"}
service<http> helloWorld {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource sayHello (http:Request request, http:Response response) {
        res:setStringPayload(response, payload);
        res:send(response, "Hello, World!");
    }
    
}
