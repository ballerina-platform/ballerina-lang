import ballerina/module1;

const STRING_VAL = "string value";
const STRING_VAL_TWO = "string value two";

public type T5 record {
    module1:a
    int field1 = 123;
};

function helloFunction() returns string {
    return "Hello Ballerina";
}

type TR1 record {
    int field1;
    int field2;
};
