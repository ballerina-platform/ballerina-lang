import ballerina.net.ws;

@ws:configuration {basePath:"/hello/{abc}/world"}
service<ws> helloWorld {

    resource onOpen(ws:Connection conn) {

    }

}
