import ballerina/module1;

const STRING_VAL = "string value";
const STRING_VAL_TWO = "string value two";

public type T5 record {
    string field0 = 
    int field1 = 123;
};

function helloFunction() returns string {
    return "Hello Ballerina";
}

type TR1 record {
    int field1;
    int field2;
};

int testIntValue = 12;

string testStringValue = "Hello World!";

function testFunctionWithReturn2() returns int {
    return 12;

}

function testFunctionWithReturn1() returns string {
    return "Hello World!";
}
