import ballerina/http;

endpoint http:NonListener echoEP {
    port:9090
};

@http:ServiceConfig {
    basePath:"/signature"
}
service<http:Service> echo bind echoEP {
    echo1 (http:Request req, http:Response res) {
        http:Response resp = new;
    }
}
