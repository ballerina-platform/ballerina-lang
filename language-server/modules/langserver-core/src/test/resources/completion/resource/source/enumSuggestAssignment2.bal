import ballerina/http;

service<http:Service> hello {
    sayHello (endpoint client, http:Request request) {
        testEnum e;
        e = t
    }
}

enum testEnum {
    ENUMERATOR1,
    ENUMERATOR2
}