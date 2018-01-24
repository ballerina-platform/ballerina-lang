import ballerina.net.http;

service<http> helloWorld {

    resource sayHello(http:InRequest req, http:InResponse res) {
        http:OutResponse resp = {};
        return resp;
    }
}
