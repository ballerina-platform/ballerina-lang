import ballerina/http;

service<http:Service> hello {
    sayHello (endpoint client, http:Request request) {
        testStruct1 myStr = {};
        myStr.
    }
}

struct testStruct1 {
    int test1A;
    string test1B;
}