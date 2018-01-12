import ballerina.net.http;

@http:configuration {basePath:"/hello"}
@http:configuration {port: 9090}
service<http> helloWorldServiceConfig {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource sayHello (http:Request request, http:Response response) {
        response.setStringPayload("Hello World !!!");
        _ = response.send();
    }
}
