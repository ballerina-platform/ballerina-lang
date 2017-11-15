import ballerina.net.ws;

@ws:configuration {basePath:"/hello/{abc}/sdfm"}
service<ws> helloWorld {

    resource onOpen(ws:Connection conn) {
        
    }

}
