import ballerina/module1;

public const int TEST_CONST = 12;

public function main() {
    map<module1:>
    int[] testArr = [];
    io:println(testArr.length());
}
