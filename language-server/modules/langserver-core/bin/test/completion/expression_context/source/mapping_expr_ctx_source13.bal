import ballerina/module1;

function testFunction() {
    int value1 = 12;
    int value2 = 20;

    TestRecord1 rec1 = {
        field1: 0,
        innerRec: {
            
        }
    };

    map<int> mapping1 = {
        field1: 12
    };

    int value3 = 1;
}

function getField() returns string {
    return "Hello World!";   
}

function getInt() returns int {
    return 1;
}

function doTask() {
    // here goes a task logic
}

type TestRecord1 record {
    int field1;
    module1:TestRecord2 innerRec;
};
