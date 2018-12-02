import ballerina/http;

service serviceName on new http:Listener(8080) {
    resource function newResource(http:Caller caller, http:Request request) {
        
    }
}

type testRecord1 record {
    int test1A = 12;
};

type testRecord2 record {
    int test2A = 12;
};