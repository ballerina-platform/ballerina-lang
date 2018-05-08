function testConstrainedMapAssignNegative() returns (map<int>) {
    map testMap;
    return testMap;
}

function testConstrainedMapRecordLiteralNegative() returns (map<int>) {
    map<int> testMap = {index_1:1, index_2:"two"};
    return testMap;
}

function testConstrainedMapIndexBasedAssignNegative() returns (map<string>) {
    map<string> testMap;
    testMap["name"] = 24;
    return testMap;
}

function testConstrainedMapAssignDifferentConstraintsNegative() returns (map<int>) {
    map<string> testMap;
    return testMap;
}

type Person {
    string name;
    int age;
    string address;
};

type Employee {
    string name;
    int age;
};

function testInvalidMapPassAsArgument() returns (map<Person>) {
    map<Employee> testMap;
    map<Person> m = returnMap(testMap);
    return m;
}

function returnMap(map<Person> m) returns (map<Person>) {
    return m;
}

function testInvalidAnyMapPassAsArgument() returns (map<Person>) {
    map testMap;
    map<Person> m = returnMap(testMap);
    return m;
}

function testInvalidStructEquivalentCast() returns (map<Person>) {
    map<Employee> testEMap;
    map<Person> testPMap;
    testPMap = <map<Person>>testEMap;
    return testPMap;
}

function testInvalidCastAnyToConstrainedMap() returns (map<Employee>) {
    map<Employee> testMap;
    any m = testMap;
    map<Employee> castMap;
    castMap = <map<Employee>>m;
    return castMap;
}

type Student {
    int index;
    int age;
};

function testInvalidStructToConstrainedMapSafeConversion() returns (map<int>) {
    Student s = {index:100, age:25};
    map<int> imap;
    imap = <map<int>>s;
    return imap;
}

function testInvalidStructEquivalentCastCaseTwo() returns (map<Student>) {
    map<Person> testPMap;
    map<Student> testSMap;
    testSMap = <map<Student>>testPMap;
    return testSMap;
}