import ballerina.net.http;

service<http:Service> helloWorld {

    resource sayHello(http:ServerConnector conn, http:Request req) {
        http:Response resp = {};
        return resp;
    }
}
