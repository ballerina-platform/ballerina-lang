import ballerina.net.ws;

@ws:configuration {basePath:"/hello"}
service<ws> helloWorld1 {
    resource onOpen(ws:Connection conn) {
        println("Hello world");
    }
}

@ws:configuration {basePath:"/hello/{abc}"}
service<ws> helloWorld2 {
    resource onOpen(ws:Connection conn) {
        println("Hello world");
    }
}

@ws:configuration {basePath:"/hello/world"}
service<ws> helloWorld3 {
    resource onOpen(ws:Connection conn) {
        println("Hello world");
    }
}

@ws:configuration {basePath:"/hello/{abc}/{xyz}"}
service<ws> helloWorld4 {
    resource onOpen(ws:Connection conn) {
        println("Hello world");
    }
}

@ws:configuration {basePath:"/hello/{abc}/world"}
service<ws> helloWorld5 {
    resource onOpen(ws:Connection conn) {
        println("Hello world");
    }
}

@ws:configuration {basePath:"/hello/{abc}+{xyz}/world"}
service<ws> helloWorld6 {
    resource onOpen(ws:Connection conn) {
        println("Hello world");
    }
}

@ws:configuration {basePath:"/hello/world/{abc}+{xyz}"}
service<ws> helloWorld7 {
    resource onOpen(ws:Connection conn) {
        println("Hello world");
    }
}
