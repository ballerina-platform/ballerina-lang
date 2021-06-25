import ballerina/module1;

public const int TEST_CONST = 12;

public function main() {
    int[] testArr = [];
}

public type TestMap2 map<string>;

public type TestMap3 map<int>;

type TestMap1 map<int>;

type TestRecord1 record {
    int rec3Field1 = 12;
    string rec3Field2 = "TestRecord3";
};

public type isolated 