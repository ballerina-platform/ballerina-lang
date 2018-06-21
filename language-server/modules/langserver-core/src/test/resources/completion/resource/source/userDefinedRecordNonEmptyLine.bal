import ballerina/http;

service<http:Service> hello {
    sayHello (endpoint client, http:Request request) {
        t
    }
}

type testRecord1 record {
    int test1A = 12;
};

type testRecord2 record {
    int test2A = 12;
};