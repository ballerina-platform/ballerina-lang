import ballerina.net.http;

service<http> helloWorld {

    resource sayHello(http:Request req, http:InResponse res) {
        http:OutResponse resp = {};
        return resp;
    }
}
