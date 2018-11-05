
type Student record {
    string name;
    string status;
    string batch;
    string school;
    !...
};

type Employee record {
    string name;
    string status;
    string batch;
};

type Person record {
    string name;
    string status;
    string batch;
    string school;
    !...
};

type Teacher record {
    string name;
    int age;
    string status;
    string batch;
    string school;
};

//-----------------------Record Seal -------------------------------------------------------------------

function testSealWithOpenRecords() returns Employee {
    Teacher t1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };

    t1.seal(Employee);
    return t1;
}

function testSealWithOpenRecordsNonAssignable() returns Teacher {
    Employee e1 = { name: "Raja", status: "single", batch: "LK2014" };

    e1.seal(Teacher);
    return e1;
}

function testSealClosedRecordWithOpenRecord() returns Employee {
    Person p1 = { name: "Raja", status: "single", batch: "LK2014", school: "Hindu College" };

    p1.seal(Employee);
    return p1;
}

function sealRecordToAny() returns any {
    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    teacher.seal(any);

    return teacher;
}

function sealRecordToJSON() returns json {

    Employee employee = { name: "John", status: "single", batch: "LK2014", school: "Hindu College" };
    employee.seal(json);

    return employee;
}

function sealRecordToMap() returns map {

    Employee employee = { name: "John", status: "single", batch: "LK2014", school: "Hindu College" };
    employee.seal(map);

    return employee;
}

function sealRecordToMapV2() returns map<string> {

    Employee employee = { name: "John", status: "single", batch: "LK2014", school: "Hindu College" };
    employee.seal(map<string>);

    return employee;
}

function sealRecordToMapV3() returns map {

    Employee employee = { name: "John", status: "single", batch: "LK2014"};
    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College", emp : employee };
    teacher.seal(map);

    return teacher;
}

//-------------------------------- Negative Test cases ------------------------------------------------------------
function sealOpenRecordToMap() returns map<string> {

    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    teacher.seal(map<string>);

    return teacher;
}

