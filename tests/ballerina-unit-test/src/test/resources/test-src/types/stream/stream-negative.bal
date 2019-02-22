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
    stream<any> testStream = new;
    return testStream;
}

function testInvalidConstrainedStreamAssignment() returns stream<int> {
    stream<string> testStream = new;
    return testStream;
}

function testStreamAsInvalidArgument() returns stream<Person> {
    stream<Employee> testStream = new;
    stream<Person> m = returnStream(testStream);
    return m;
}

function testAnyStreamAsInvalidArgument() returns stream<Person> {
    stream<any> testStream = new;
    stream<Person> m = returnStream(testStream);
    return m;
}

function returnStream(stream<Person> m) returns stream<Person> {
    return m;
}
