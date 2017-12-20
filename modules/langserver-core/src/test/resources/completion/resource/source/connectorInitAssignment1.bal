import ballerina.net.http;

service<http> service1 {
    resource echo1 (http:Request req,http:Response res) {
        http:HttpClient c;
        c = create http:
    }
}