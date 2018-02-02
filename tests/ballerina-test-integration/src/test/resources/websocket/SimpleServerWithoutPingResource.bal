import ballerina.net.ws;

@ws:configuration {
    basePath: "/test/without/ping/resource",
    port:9090
}
service<ws> SimpleProxyServer {
    resource onOpen(ws:Connection conn) {
        println("New Client Connected");
    }
}
