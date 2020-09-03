import ballerina/module1;

function testFunction() {
    int value1 = 12;
    int value2 = 20;
    
    module1:TestRecord2 moduleRec = {
        rec2Field1: 1  
    };

    TestRecord1 rec1 = {
        field1: 0
    };

    map<int> mapping1 = {
        field1: 12
    };

    TestRecord2 rec = {
        
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

type TestRecord2 record {
    int field1;
    string field2 = "";
    boolean field3 = false;
};

type TestRecord1 record {
    int field1;
};
