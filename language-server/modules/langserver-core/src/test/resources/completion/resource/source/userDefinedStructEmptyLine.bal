import ballerina/http;

service<http:Service> hello {
    sayHello (endpoint client, http:Request request) {
        
    }
}

struct testStruct1 {
    int test1A = 12;
}

struct testStruct2 {
    int test2A = 12;
}