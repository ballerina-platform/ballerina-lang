function testFunction() {
    string fullName = getFullName("John", "Doe");
}

function getFullName(string firstName, string lastName) {
    return firstName + " " + lastName.substring(0, 1);
}

type Person record {|
    string name;
    int age;
|};

type Params record {|
    *Person;
    decimal salary;
|};

public function main() returns error? {
    test("hello", 1, 2);
    testIncludedRecordParams(true, {
        name: "",
        salary: 0,
        age: 0
    }, "restarg1", "restarg2");

    testIncludedRecordParams(true, age = 20);

}

public function test(string arg1, int... restArg) {

}

public function testIncludedRecordParams(boolean param1, *Params params, string... rest) {

}
