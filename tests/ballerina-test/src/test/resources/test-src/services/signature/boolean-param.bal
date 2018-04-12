import ballerina/http;

endpoint http:NonListener echoEP {
    port:9090
};

@http:ServiceConfig {
    basePath:"/signature"
}
service<http:Service> echo bind echoEP {
    echo1 (endpoint conn, http:Request req, boolean key) {
        http:Response res = new;
    }
}
