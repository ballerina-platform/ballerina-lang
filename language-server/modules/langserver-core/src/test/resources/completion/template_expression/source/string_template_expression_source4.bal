import ballerina/module1;

public function main() {
    int varInt = 1;
    string varString = "Hello";
    IntTypeDef varIntTypeDef = 1;
    RecordTypeDec varRecTypeDef = {
        field1: 0,
        field2: 0
    };

    string hello = string `test ${}`;
}

function functionwithIntReturn() returns int {
    return 123;
}

function functionwithRecordTypeDecReturn() returns RecordTypeDec {
    return {
        field1: 0,
        field2: 0
    };
}

type IntTypeDef int;

type RecordTypeDec record {
    int field1;
    int field2;
};
