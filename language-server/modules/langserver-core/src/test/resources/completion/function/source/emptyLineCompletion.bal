import ballerina/io;

final int final1 = 0;

const string const2 = "test const";

function function1(int param1,int param2) {
    int var1 = 1;
    int var2 = 2;
    
}

type testRecord1 record {
    int test1A = 12;
};

type testRecord2 record {
    int test2A = 12;
};

function function3(int a, string b) {
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
