import ballerina/module1;

function testFunction() {
    int|string|float testVar = 123;
    boolean tested = testVar is module1:
}

public type TestRecord1 record {
    int field1 = 1;
    int field2 = 2;
};
