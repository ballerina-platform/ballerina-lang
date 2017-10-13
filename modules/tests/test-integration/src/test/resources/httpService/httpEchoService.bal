import ballerina.net.http;

@http:configuration {
    basePath:"/echo",
    port:9094
}
service<http> echo {

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource echo (http:Request req, http:Response res) {
        res.setStringPayload("hello world");
        res.send();
    
    }
    
}

@http:configuration {
    basePath:"/echoOne",
    port:9094
}
service<http> echoOne {

    @http:resourceConfig {
        methods:["POST"],
        path:"/abc"
    }
    resource echoAbc (http:Request req, http:Response res) {
        res.setStringPayload("hello world");
        res.send();

    }
}

@http:configuration {
    basePath:"/echoDummy"
}
service<http> echoDummy {

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource echoDummy (http:Request req, http:Response res) {
        res.setStringPayload("hello world");
        res.send();

    }

}
