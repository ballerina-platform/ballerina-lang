import ballerina.net.http;

@http:configuration {basePath:"/hello"}
service<http> helloWorld {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource sayHello (http:Request req, http:Response res) {
        res.setStringPayload("Hello, World!");
        res.send();
    }
    
}
