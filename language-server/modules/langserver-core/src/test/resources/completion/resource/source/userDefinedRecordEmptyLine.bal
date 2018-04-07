import ballerina/http;

service<http:Service> hello {
    sayHello (endpoint client, http:Request request) {
        
    }
}

type testRecord1 {
    int test1A = 12;
};

type testRecord2 {
    int test2A = 12;
};