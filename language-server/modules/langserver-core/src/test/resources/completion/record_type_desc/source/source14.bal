import ballerina/module1;

const STRING_VAL = "string value";
const STRING_VAL_TWO = "string value two";

public type T5 record {
    int f
};

function helloFunction() returns string {
    return "Hello Ballerina";
}

type TR1 record {
    int field1;
    int field2;
};

public class TestClass {
    int classField1;
}

public type TestType1 TestClass;

public type TestType2 TR1;
