function testConstrainedMapAssignNegative() returns (map<int>) {
    map<any> testMap = {};
    return testMap;
}

function testConstrainedMapRecordLiteralNegative() returns (map<int>) {
    map<int> testMap = {index_1:1, index_2:"two"};
    return testMap;
}

function testConstrainedMapIndexBasedAssignNegative() returns (map<string>) {
    map<string> testMap = {};
    testMap["name"] = 24;
    return testMap;
}

function testConstrainedMapAssignDifferentConstraintsNegative() returns (map<int>) {
    map<string> testMap = {};
    return testMap;
}

type Person record {
    string name;
    int age;
    string address;
};

type Employee record {
    string name;
    int age;
};

function testInvalidMapPassAsArgument() returns (map<Person>) {
    map<Employee> testMap = {};
    map<Person> m = returnMap(testMap);
    return m;
}

function returnMap(map<Person> m) returns (map<Person>) {
    return m;
}

function testInvalidAnyMapPassAsArgument() returns (map<Person>) {
    map<any> testMap = {};
    map<Person> m = returnMap(testMap);
    return m;
}

function testInvalidStructEquivalentCast() returns (map<Person>) {
    map<Employee> testEMap = {};
    map<Person> testPMap = <map<Person>>testEMap;
    return testPMap;
}

function testInvalidCastAnyToConstrainedMap() returns (map<Employee>) {
    map<Employee> testMap = {};
    any m = testMap;
    map<Employee> castMap = <map<Employee>>m;
    return castMap;
}

type Student record {
    int index;
    int age;
};

function testInvalidStructToConstrainedMapSafeConversion() returns (map<int>|error) {
    Student s = {index:100, age:25};
    map<int> imap = check s.cloneWithType(map<int>);
    return imap;
}

function testInvalidStructEquivalentCastCaseTwo() returns (map<Student>) {
    map<Person> testPMap = {};
    map<Student> testSMap = <map<Student>>testPMap;
    return testSMap;
}

function testMapToStructConversionNegative () returns (Student|error) {
    map<string> testMap = {};
    testMap["index"] = "100";
    testMap["age"] = "63";
    return check testMap.cloneWithType(typedesc<Student>);
}
