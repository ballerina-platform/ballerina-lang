import ballerina/log;
import ballerina/time;
import ballerina/runtime;

stream<Employee> globalEmployeeStream = new;

type Employee record {
    int id = 0;
    string name = "";
};

type Person record {
    int id;
    string name;
};

type Job record {
    string description;
};

type Captain object {
    private string name;
    private int id;

    function __init(string name, int id) {
        self.name = name;
        self.id = id;
    }

    function logName() {
        log:printInfo(self.name);
    }
};

type Member object {
    private string name;
    private int id;

    function __init(string name, int id) {
        self.name = name;
        self.id = id;
    }

    function logName() {
        log:printInfo(self.name);
    }
};

type Coach object {
    private string name;
    private int registrationId;
    private float salary;

    function __init(string name, int registrationId, float salary) {
        self.name = name;
        self.registrationId = registrationId;
        self.salary = salary;
    }

    public function logName() {
        log:printInfo(self.name);
    }
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

function testInvalidObjectPublishingToStream() {
    stream<Captain> s1 = new;
    Coach c1 = new("Maryam", 120384, 1000.0);
    s1.publish(c1);
}

function testSubscriptionFunctionWithIncorrectObjectParameter() {
    stream<Captain> s1 = new;
    s1.subscribe(printCoachName);
}

function testSubscriptionFunctionWithUnassignableUnionParameter() {
    stream<int[]|string|boolean|float> unionStream = new;
    unionStream.subscribe(addToGlobalAnyArrayForUnionType);
}

function testSubscriptionFunctionWithUnassignableTupleTypeParameter() {
    stream<(int, float)> tupleStream = new;
    tupleStream.subscribe(addToGlobalAnyArrayForTupleType);
}

int arrayIndex = 0;
Employee globalEmployee = {};

function testGlobalStream() returns (Employee, Employee, Employee) {
    Employee origEmployee = globalEmployee;
    globalEmployeeStream.subscribe(assignGlobalEmployee);
    Employee publishedEmployee = { id:5678, name:"Maryam" };
    globalEmployeeStream.publish(publishedEmployee);
    int startTime = time:currentTime().time;

    //allow for value update
    while (globalEmployee.id == 0 && time:currentTime().time - startTime < 1000) {
        runtime:sleep(100);
    }
    return (origEmployee, publishedEmployee, globalEmployee);
}

function testStreamPublishingAndSubscriptionForRecord() returns (Employee, Employee, Employee) {
    Employee origEmployee = globalEmployee;
    stream<Employee> s1 = new;
    s1.subscribe(assignGlobalEmployee);
    Employee publishedEmployee = { id:1234, name:"Maryam" };
    s1.publish(publishedEmployee);
    int startTime = time:currentTime().time;

    //allow for value update
    while (globalEmployee.id == 0 && time:currentTime().time - startTime < 1000) {
        runtime:sleep(100);
    }
    return (origEmployee, publishedEmployee, globalEmployee);
}

Employee[] globalEmployeeArray = [];

function testStreamPublishingAndSubscriptionForMultipleRecordEvents() returns (Employee[], Employee[]) {
    arrayIndex = 0;
    stream<Employee> s1 = new;
    s1.subscribe(addToGlobalEmployeeArray);
    Employee e1 = { id:1234, name:"Maryam" };
    Employee e2 = { id:2345, name:"Aysha" };
    Employee e3 = { id:3456, name:"Sumayya" };
    Employee[] publishedEmployees = [e1, e2, e3];
    s1.publish(e1);
    s1.publish(e2);
    s1.publish(e3);
    int startTime = time:currentTime().time;

    //allow for value update
    while (globalEmployeeArray.length() < 3 && time:currentTime().time - startTime < 5000) {
        runtime:sleep(100);
    }
    return (publishedEmployees, globalEmployeeArray);
}

int[] globalIntegerArray = [];

function testStreamPublishingAndSubscriptionForIntegerStream() returns (int[], int[]) {
    arrayIndex = 0;
    stream<int> intStream = new;
    intStream.subscribe(addToGlobalIntegerArray);
    int[] publishedIntegerEvents = [11, 24857, 0, -1, 999];
    foreach var intEvent in publishedIntegerEvents {
        intStream.publish(intEvent);
    }
    int startTime = time:currentTime().time;

    //allow for value update
    while (globalIntegerArray.length() < publishedIntegerEvents.length() && time:currentTime().time - startTime < 5000) {
        runtime:sleep(100);
    }
    return (publishedIntegerEvents, globalIntegerArray);
}

boolean[] globalBooleanArray = [];

function testStreamPublishingAndSubscriptionForBooleanStream() returns (boolean[], boolean[]) {
    arrayIndex = 0;
    stream<boolean> booleanStream = new;
    booleanStream.subscribe(addToGlobalBooleanArray);
    boolean[] publishedBooleanEvents = [true, false, false, true, false];
    foreach var booleanEvent in publishedBooleanEvents {
        booleanStream.publish(booleanEvent);
    }
    int startTime = time:currentTime().time;

    //allow for value update
    while (globalBooleanArray.length() < publishedBooleanEvents.length() && time:currentTime().time - startTime < 5000) {
        runtime:sleep(100);
    }
    return (publishedBooleanEvents, globalBooleanArray);
}

any[] globalAnyArray = [];

function testStreamPublishingAndSubscriptionForUnionTypeStream() returns (any[], any[]) {
    globalAnyArray = [];
    arrayIndex = 0;
    stream<int[]|string|boolean> unionStream = new;
    unionStream.subscribe(addToGlobalAnyArrayForUnionType);
    int[] intarray = [1, 2, 3];
    any[] publishedEvents = [intarray, "Maryam", false];
    foreach var event in publishedEvents {
        unionStream.publish(event);
    }
    int startTime = time:currentTime().time;

    //allow for value update
    while (globalAnyArray.length() < publishedEvents.length() && time:currentTime().time - startTime < 5000) {
        runtime:sleep(100);
    }
    return (publishedEvents, globalAnyArray);
}

function testStreamPublishingAndSubscriptionForAssignableUnionTypeStream(int intVal) returns (any[], any[]) {
    globalAnyArray = [];
    arrayIndex = 0;
    stream<string|boolean|int|int[]> unionStream = new;
    unionStream.subscribe(addToGlobalAnyArrayForAssignableUnionType);
    int[] intarray = [1, 2, 3];
    any[] publishedEvents = [intarray, "Maryam", false, intVal];
    foreach var event in publishedEvents {
        unionStream.publish(event);
    }
    int startTime = time:currentTime().time;

    //allow for value update
    while (globalAnyArray.length() < publishedEvents.length() && time:currentTime().time - startTime < 5000) {
        runtime:sleep(100);
    }
    return (publishedEvents, globalAnyArray);
}

function testStreamPublishingAndSubscriptionForTupleTypeStream() returns (any[], any[]) {
    globalAnyArray = [];
    arrayIndex = 0;
    stream<(string, int)> tupleStream = new;
    tupleStream.subscribe(addToGlobalAnyArrayForTupleType);
    (string, int) tuple = ("tuple1", 1234);
    (string, int) tuple2 = ("tuple2", 9876);
    any[] publishedEvents = [tuple, tuple2];
    foreach var event in publishedEvents {
        tupleStream.publish(event);
    }
    int startTime = time:currentTime().time;

    //allow for value update
    while (globalAnyArray.length() < publishedEvents.length() && time:currentTime().time - startTime < 5000) {
        runtime:sleep(100);
    }
    return (publishedEvents, globalAnyArray);
}

function testStreamPublishingAndSubscriptionForAssignableTupleTypeStream(string s1, int i1, string s2, int i2) returns
                                                                                                               any[] {
    globalAnyArray = [];
    arrayIndex = 0;
    stream<(string, int)> tupleStream = new;
    tupleStream.subscribe(addToGlobalAnyArrayForAssignableTupleType);
    (string, int)[] publishedEvents = [(s1, i1), (s2, i2)];
    foreach var event in publishedEvents {
        tupleStream.publish(event);
    }
    int startTime = time:currentTime().time;

    //allow for value update
    while (globalAnyArray.length() / 2 < publishedEvents.length() && time:currentTime().time - startTime < 5000) {
        runtime:sleep(100);
    }
    return globalAnyArray;
}

function testStreamPublishingAndSubscriptionForAnyTypeStream() returns (any[], any[]) {
    globalAnyArray = [];
    arrayIndex = 0;
    stream<any> anyStream = new;
    anyStream.subscribe(addToGlobalAnyArrayForAnyType);
    (string, int) tuple = ("anyStream", 1234);
    any[] publishedEvents = [tuple, "any", false, 0.5];
    foreach var event in publishedEvents {
        anyStream.publish(event);
    }
    int startTime = time:currentTime().time;

    //allow for value update
    while (globalAnyArray.length() < publishedEvents.length() && time:currentTime().time - startTime < 5000) {
        runtime:sleep(100);
    }
    return (publishedEvents, globalAnyArray);
}

function testStreamsPublishingForStructurallyEquivalentRecords() returns (any[], any[]) {
    globalEmployeeArray = [];
    arrayIndex = 0;
    stream<Employee> employeeStream = new;
    employeeStream.subscribe(addPersonToGlobalEmployeeArray);
    Person p1 = { id:3000, name:"Maryam" };
    Person p2 = { id:3003, name:"Ziyad" };
    Person[] publishedEvents = [p1, p2];
    foreach var event in publishedEvents {
        employeeStream.publish(event);
    }
    int startTime = time:currentTime().time;

    //allow for value update
    while (globalEmployeeArray.length() < publishedEvents.length() && time:currentTime().time - startTime < 5000) {
        runtime:sleep(100);
    }
    return (publishedEvents, globalEmployeeArray);
}

Member?[] globalMemberArray = [];

function testStreamsPublishingForStructurallyEquivalentObjects() returns (any[], any[]) {
    globalMemberArray = [];
    arrayIndex = 0;
    stream<Member> memberStream = new;
    memberStream.subscribe(addCaptainToGlobalMemberArray);
    Captain c1 = new("Maryam", 123456);
    Captain c2 = new("Ziyad", 654321);
    Captain?[] publishedCaptains = [c1, c2];
    foreach var event in publishedCaptains {
        memberStream.publish(event);
    }
    int startTime = time:currentTime().time;

    //allow for value update
    while (globalMemberArray.length() < publishedCaptains.length() && time:currentTime().time - startTime < 5000) {
        runtime:sleep(100);
    }
    return (publishedCaptains, globalMemberArray);
}

function printJobDescription(Job j) {
    log:printInfo(j.description);
}

function printCoachName(Coach c) {
    c.logName();
}

function assignGlobalEmployee (Employee e) {
    globalEmployee = e;
}

function addPersonToGlobalEmployeeArray(Person p) {
    globalEmployeeArray[arrayIndex] = p;
    arrayIndex = arrayIndex + 1;
}

function addToGlobalEmployeeArray (Employee e) {
    globalEmployeeArray[arrayIndex] = e;
    arrayIndex = arrayIndex + 1;
}

function addCaptainToGlobalMemberArray (Captain c) {
    globalMemberArray[arrayIndex] = c;
    arrayIndex = arrayIndex + 1;
}

function addToGlobalBooleanArray (boolean b) {
    globalBooleanArray[arrayIndex] = b;
    arrayIndex = arrayIndex + 1;
}

function addToGlobalIntegerArray (int i) {
    globalIntegerArray[arrayIndex] = i;
    arrayIndex = arrayIndex + 1;
}

function addToGlobalAnyArrayForUnionType(int[]|string|boolean val) {
    globalAnyArray[arrayIndex] = val;
    arrayIndex = arrayIndex + 1;
}

function addToGlobalAnyArrayForAssignableUnionType(string|boolean|int|int[]|boolean[] val) {
    globalAnyArray[arrayIndex] = val;
    arrayIndex = arrayIndex + 1;
}

function addToGlobalAnyArrayForTupleType((string, int) val) {
    globalAnyArray[arrayIndex] = val;
    arrayIndex = arrayIndex + 1;
}

function addToGlobalAnyArrayForAssignableTupleType((json, float) val) {
    json jsonVal;
    float floatVal;
    (jsonVal, floatVal) = val;
    globalAnyArray[arrayIndex] = jsonVal;
    arrayIndex = arrayIndex + 1;
    globalAnyArray[arrayIndex] = floatVal;
    arrayIndex = arrayIndex + 1;
}

function addToGlobalAnyArrayForAnyType (any val) {
    globalAnyArray[arrayIndex] = val;
    arrayIndex = arrayIndex + 1;
}
