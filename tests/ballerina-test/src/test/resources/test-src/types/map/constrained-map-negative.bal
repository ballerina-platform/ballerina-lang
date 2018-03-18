function testConstrainedMapAssignNegative() (map<int>) {
    map testMap = {};
    return testMap;
}

function testConstrainedMapRecordLiteralNegative() (map<int>) {
    map<int> testMap = {index_1:1, index_2:"two"};
    return testMap;
}

function testConstrainedMapIndexBasedAssignNegative() (map<string>) {
    map<string> testMap = {};
    testMap["name"] = 24;
    return testMap;
}

function testConstrainedMapAssignDifferentConstraintsNegative() (map<int>) {
    map<string> testMap = {};
    return testMap;
}

struct Person {
    string name;
    int age;
    string address;
}

struct Employee {
    string name;
    int age;
}

function testInvalidMapPassAsArgument() (map<Person>) {
    map<Employee> testMap = {};
    map<Person> m = returnMap(testMap);
    return m;
}

function returnMap(map<Person> m) (map<Person>) {
    return m;
}

function testInvalidAnyMapPassAsArgument() (map<Person>) {
    map testMap = {};
    map<Person> m = returnMap(testMap);
    return m;
}

function testInvalidStructEquivalentCast() (map<Person>) {
    map<Employee> testEMap = {};
    map<Person> testPMap;
    testPMap,_ = (map<Person>)testEMap;
    return testPMap;
}

function testInvalidCastAnyToConstrainedMap() (map<Employee>) {
    map<Employee> testMap = {};
    any m = testMap;
    map<Employee> castMap;
    castMap,_ = (map<Employee>)m;
    return castMap;
}

struct Student {
    int index;
    int age;
}

function testInvalidStructToConstrainedMapSafeConversion() (map<int>) {
    Student s = {index:100, age:25};
    map<int> imap;
    imap = <map<int>>s;
    return imap;
}

function testInvalidStructEquivalentCastCaseTwo() (map<Student>) {
    map<Person> testPMap = {};
    map<Student> testSMap;
    testSMap,_ = (map<Student>)testPMap;
    return testSMap;
}