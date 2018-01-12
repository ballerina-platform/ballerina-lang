import ballerina.net.http;

@http:configuration {basePath:"/hello"}
service<http> helloWorldResourceConfig {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    @http:resourceConfig {
        methods:["POST"]
    }
    resource sayHello (http:Request request, http:Response response) {
        response.setStringPayload("Hello World !!!");
        _ = response.send();
    }
}
