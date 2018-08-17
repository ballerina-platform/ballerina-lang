type Person record {
    string name;
    int age;
    string address;
};

type Employee record {
    string name;
    int age;
};

function testInvalidStreamAssignment() returns stream<int> {
    stream testStream;
    return testStream;
}

function testInvalidConstrainedStreamAssignment() returns stream<int> {
    stream<string> testStream;
    return testStream;
}

function testStreamAsInvalidArgument() returns stream<Person> {
    stream<Employee> testStream;
    stream<Person> m = returnStream(testStream);
    return m;
}

function testAnyStreamAsInvalidArgument() returns stream<Person> {
    stream testStream;
    stream<Person> m = returnStream(testStream);
    return m;
}

function returnStream(stream<Person> m) returns stream<Person> {
    return m;
}

function testInvalidObjectConstrainedStreamCast() returns stream<Person> {
    stream<Employee> testEmployeeStream;
    stream<Person> testPersonStream;
    testPersonStream = <stream<Person>>testEmployeeStream;
    return testPersonStream;
}

function testInvalidObjectToConstrainedStreamCast() returns stream<int> {
    Employee s = {name:"Anne", age:25};
    stream<int> intStream;
    intStream = <stream<int>>s;
    return intStream;
}

function testInvalidConstrainedStreamCast() returns stream<Employee> {
    stream<Person> testPersonStream;
    stream<Employee> testEmployeeStream;
    testEmployeeStream = <stream<Employee>>testPersonStream;
    return testEmployeeStream;
}

function testInvalidAnyToConstrainedStreamCast() returns stream<Employee> {
    stream<Employee> testStream;
    any anyValue = testStream;
    stream<Employee> castStream;
    castStream = <stream<Employee>>anyValue;
    return castStream;
}
