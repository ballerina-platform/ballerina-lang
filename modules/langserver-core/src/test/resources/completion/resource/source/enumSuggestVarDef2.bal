import ballerina.net.http;

service<http> service1 {
    resource echo1 (http:Request req,http:Response res) {
        testEnum e = t
    }
}

enum testEnum {
    ENUMERATOR1,
    ENUMERATOR2
}