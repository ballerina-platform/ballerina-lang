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

type Member object {
    public string name;
    public int id;

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

int arrayIndex = 0;
Employee globalEmployee = {};

function testGlobalStream() returns [Employee, Employee, Employee] {
    Employee origEmployee = globalEmployee;
    globalEmployeeStream.subscribe(assignGlobalEmployee);
    Employee publishedEmployee = { id:5678, name:"Maryam" };
    globalEmployeeStream.publish(publishedEmployee);
    int startTime = time:currentTime().time;

    //allow for value update
    while (globalEmployee.id == 0 && time:currentTime().time - startTime < 1000) {
        runtime:sleep(100);
    }
    return [origEmployee, publishedEmployee, globalEmployee];
}

function testStreamPublishingAndSubscriptionForRecord() returns [Employee, Employee, Employee] {
    globalEmployee = {};
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
    return [origEmployee, publishedEmployee, globalEmployee];
}

function testRecordPublishingToStreamArray() returns [Employee, Employee, Employee] {
    globalEmployee = {};
    Employee origEmployee = globalEmployee;
    stream<Employee>[] s1 = [];
    s1[1] = new; //Initialize 1st element in the array, so 0th element will be also filled with filler value.

    s1[0].subscribe(assignGlobalEmployee);
    Employee publishedEmployee = { id:1234, name:"Maryam" };
    s1[0].publish(publishedEmployee);
    int startTime = time:currentTime().time;

    //allow for value update
    while (globalEmployee.id == 0 && time:currentTime().time - startTime < 1000) {
        runtime:sleep(100);
    }
    return [origEmployee, publishedEmployee, globalEmployee];
}

Employee[] globalEmployeeArray = [];

function testStreamPublishingAndSubscriptionForMultipleRecordEvents() returns [Employee[], Employee[]] {
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
    return [publishedEmployees, globalEmployeeArray];
}

int[] globalIntegerArray = [];

function testStreamPublishingAndSubscriptionForIntegerStream() returns [int[], int[]] {
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
    return [publishedIntegerEvents, globalIntegerArray];
}

boolean[] globalBooleanArray = [];

function testStreamPublishingAndSubscriptionForBooleanStream() returns [boolean[], boolean[]] {
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
    return [publishedBooleanEvents, globalBooleanArray];
}

anydata[] globalAnydataArray = [];

function testStreamPublishingAndSubscriptionForUnionTypeStream() returns boolean {
    globalAnydataArray = [];
    arrayIndex = 0;
    stream<int[]|string|boolean> unionStream = new;
    unionStream.subscribe(addToGlobalAnydataArrayForUnionType);
    int[] intarray = [1, 2, 3];
    (int[]|string|boolean)[] publishedEvents = [intarray, "Maryam", false];
    foreach var event in publishedEvents {
        unionStream.publish(event);
    }
    int startTime = time:currentTime().time;

    //allow for value update
    while (globalAnydataArray.length() < publishedEvents.length() && time:currentTime().time - startTime < 5000) {
        runtime:sleep(100);
    }
    return publishedEvents == globalAnydataArray;
}

function testStreamPublishingAndSubscriptionForAssignableUnionTypeStream(int intVal) returns boolean {
    globalAnydataArray = [];
    arrayIndex = 0;
    stream<string|boolean|int|int[]> unionStream = new;
    unionStream.subscribe(addToGlobalAnydataArrayForAssignableUnionType);
    int[] intarray = [1, 2, 3];
    (string|boolean|int|int[])[] publishedEvents = [intarray, "Maryam", false, intVal];
    foreach var event in publishedEvents {
        unionStream.publish(event);
    }
    int startTime = time:currentTime().time;

    //allow for value update
    while (globalAnydataArray.length() < publishedEvents.length() && time:currentTime().time - startTime < 5000) {
        runtime:sleep(100);
    }
    return publishedEvents == globalAnydataArray;
}

function testStreamPublishingAndSubscriptionForTupleTypeStream() returns boolean {
    globalAnydataArray = [];
    arrayIndex = 0;
    stream<[string, int]> tupleStream = new;
    tupleStream.subscribe(addToGlobalAnydataArrayForTupleType);
    [string, int] tuple = ["tuple1", 1234];
    [string, int] tuple2 = ["tuple2", 9876];
    [string, int][] publishedEvents = [tuple, tuple2];
    foreach var event in publishedEvents {
        tupleStream.publish(event);
    }
    int startTime = time:currentTime().time;

    //allow for value update
    while (globalAnydataArray.length() < publishedEvents.length() && time:currentTime().time - startTime < 5000) {
        runtime:sleep(100);
    }
    return publishedEvents == globalAnydataArray;
}

function testStreamPublishingAndSubscriptionForAssignableTupleTypeStream(string s1, int i1, string s2, int i2) returns
                                                                                                               any[] {
    globalAnydataArray = [];
    arrayIndex = 0;
    stream<[string, int]> tupleStream = new;
    tupleStream.subscribe(addToGlobalAnydataArrayForAssignableTupleType);
    [string, int][] publishedEvents = [[s1, i1], [s2, i2]];
    foreach var event in publishedEvents {
        tupleStream.publish(event);
    }
    int startTime = time:currentTime().time;

    //allow for value update
    while (globalAnydataArray.length() / 2 < publishedEvents.length() && time:currentTime().time - startTime < 5000) {
        runtime:sleep(100);
    }
    return globalAnydataArray;
}

function testStreamPublishingAndSubscriptionForAnydataTypeStream() returns boolean {
    globalAnydataArray = [];
    arrayIndex = 0;
    stream<anydata> anydataStream = new;
    anydataStream.subscribe(addToGlobalAnydataArrayForAnyType);
    [string, int] tuple = ["anyStream", 1234];
    anydata[] publishedEvents = [tuple, "any", false, 0.5];
    foreach var event in publishedEvents {
        anydataStream.publish(event);
    }
    int startTime = time:currentTime().time;

    //allow for value update
    while (globalAnydataArray.length() < publishedEvents.length() && time:currentTime().time - startTime < 5000) {
        runtime:sleep(100);
    }
    return publishedEvents == globalAnydataArray;
}

function testStreamsPublishingForStructurallyEquivalentRecords() returns boolean {
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
    return publishedEvents == globalEmployeeArray;
}

function testStreamViaFuncArg(stream<Employee> employeeStream) returns boolean {
    globalEmployeeArray = [];
    arrayIndex = 0;
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
    return publishedEvents == globalEmployeeArray;
}

function testStreamPublishingInitStreamViaFuncArgs() returns boolean {
    return testStreamViaFuncArg(new);
}

function testStreamEventClone() returns Employee[] {
    arrayIndex = 0;
    stream<Employee> s1 = new;
    Employee[] arr = [];

    s1.subscribe(function(Employee e) {
        e.name = "CloneOfGima";
        arr[arr.length()] = e;
    });

    Employee e1 = { id:1234, name:"Gima" };
    arr[0] = e1;

    s1.publish(e1);

    int startTime = time:currentTime().time;

    //allow for value update
    while (globalEmployeeArray.length() < 2 && time:currentTime().time - startTime < 5000) {
        runtime:sleep(100);
    }

    return arr;
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

function addToGlobalBooleanArray (boolean b) {
    globalBooleanArray[arrayIndex] = b;
    arrayIndex = arrayIndex + 1;
}

function addToGlobalIntegerArray (int i) {
    globalIntegerArray[arrayIndex] = i;
    arrayIndex = arrayIndex + 1;
}

function addToGlobalAnydataArrayForUnionType(int[]|string|boolean val) {
    globalAnydataArray[arrayIndex] = val;
    arrayIndex = arrayIndex + 1;
}

function addToGlobalAnydataArrayForAssignableUnionType(string|boolean|int|int[]|boolean[] val) {
    globalAnydataArray[arrayIndex] = val;
    arrayIndex = arrayIndex + 1;
}

function addToGlobalAnydataArrayForTupleType([string, int] val) {
    globalAnydataArray[arrayIndex] = val;
    arrayIndex = arrayIndex + 1;
}

function addToGlobalAnydataArrayForAssignableTupleType([json, int] val) {
    json jsonVal;
    int intVal;
    [jsonVal, intVal] = val;
    globalAnydataArray[arrayIndex] = jsonVal;
    arrayIndex = arrayIndex + 1;
    globalAnydataArray[arrayIndex] = intVal;
    arrayIndex = arrayIndex + 1;
}

function addToGlobalAnydataArrayForAnyType(anydata val) {
    globalAnydataArray[arrayIndex] = val;
    arrayIndex = arrayIndex + 1;
}
