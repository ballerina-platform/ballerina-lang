import ballerina/module1;

public const int TEST_CONST = 12;

public function main() {
    int[] testArr = [];
    int testValue1 = 12;
}

type TableRecord record {
    string name;
    int age;
    table<Country> 
}

type Country record {
    string country;
    int town;
}
