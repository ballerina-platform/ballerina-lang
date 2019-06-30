import ballerina/http;

final int const1 = 0;

const string const2 = "test const";

service serviceName on new http:Listener(8080) {
    resource function newResource(http:Caller caller, http:Request request) {
        int var1 = 12;
        int var2 = 123;
        
    }
}

function function1(int a, string b) {
    int testVal = a;
}

function function2() returns (int){
    int testA = 1;
    int testB = 2;
    return testA;
}

function function4() {
    string testStr = "This is Test String";
}

type testRecord1 record {
    int test1A = 12;
};

type testRecord2 record {
    int test2A = 12;
};
