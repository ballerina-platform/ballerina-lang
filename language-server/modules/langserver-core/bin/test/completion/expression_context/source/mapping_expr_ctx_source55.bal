import ballerina/module1;

public const TEST_CONST = 124;

function testFunction() {
    int intVar1 = 123;
    string stringVar1 = "";

    TestRecord rec1 = {
        intField: 0,
        stringField: ""
    };
    
    TestRecord2 rec2 = {
        intField: 0,
        stringField: ""
    };

    module1:TestRecord1 rec3 = {
        rec1Field1: 0,
        rec1Field2: ""
    };
    int anIntVar = 12;
    map<string> mapVar1 = {};
    map<string> mapVar2 = {...};
} 

type TestRecord record {
    int intField = 12;
    string stringField;
};

public type TestRecord2 TestRecord;

function getAnIntValue() returns int {
    return 1234;
}

function getMapValue() returns map<string> {
    map<string> m = {};

    return m;
}
