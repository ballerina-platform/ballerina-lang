import ballerina.net.http;

service<http> service1 {
    resource echo1 (http:Connection conn, http:Request inRequest) {
        testEnum e = t
    }
}

enum testEnum {
    ENUMERATOR1,
    ENUMERATOR2
}