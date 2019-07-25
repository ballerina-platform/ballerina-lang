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
    stream<anydata> testStream = new;
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

function testAnydataStreamAsInvalidArgument() returns stream<Person> {
    stream<anydata> testStream = new;
    stream<Person> m = returnStream(testStream);
    return m;
}

function returnStream(stream<Person> m) returns stream<Person> {
    return m;
}

type Captain object {
    public string name;

    function __init(string name) {
        self.name = name;
    }
};

stream<Captain> s1 = new;

type Job record {
    string description;
};

function testInvalidRecordPublishingToStream() {
    stream<Employee> s1 = new;
    Job j1 = { description:"Dummy Description 1" };
    s1.publish(j1);
}

function testSubscriptionFunctionWithIncorrectRecordParameter() {
    stream<Employee> s1 = new;
    s1.subscribe(printJobDescription);
}

function testSubscriptionFunctionWithUnassignableTupleTypeParameter() {
    stream<[int, float]> tupleStream = new;
    tupleStream.subscribe(addToGlobalAnydataArrayForTupleType);
}

function testSubscriptionFunctionWithUnassignableUnionParameter() {
    stream<int[]|string|boolean|float> unionStream = new;
    unionStream.subscribe(addToGlobalAnydataArrayForUnionType);
}

function printJobDescription(Job j) {
}

function addToGlobalAnydataArrayForTupleType([string, int] val) {
}

function addToGlobalAnydataArrayForUnionType(int[]|string|boolean val) {
}
