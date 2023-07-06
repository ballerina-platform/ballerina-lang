import ballerina/module1;

public const int TEST_CONST = 12;

public function main() {
    TestRecord[] testArr = [];
    io:println(testArr.length());
}

type TestRecord record {
    int testField
};
