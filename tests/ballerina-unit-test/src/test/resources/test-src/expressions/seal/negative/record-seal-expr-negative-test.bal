type Employee record {
    string name;
    int age;
    float salary;
};

type Student record {
    string name;
    int age;
    string batch;
    !...
};

type Person record {
    string name;
    int age;
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

type TeacherObj object {
    string name;
    int age;
    string status;
    string batch;
    string school;
};

function sealRecordToXML() returns xml {

    Employee employeeRecord = { name: "Raja", age: 25, salary: 20000 };

    employeeRecord.seal(xml);
    return employeeRecord;
}

function sealOpenRecordToClosedRecord() returns Employee {

    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    teacher.seal(Employee);

    return teacher;
}

function sealClosedRecordToClosedRecord() returns Student {

    Person person = { name: "Raja", age: 25, batch: "LK2014", school: "Hindu College" };
    person.seal(Student);

    return person;
}

function sealRecordToObject() returns TeacherObj {

    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    teacher.seal(TeacherObj);

    return teacher;
}

function sealClosedRecordToMap() returns map<string> {

    Person person = { name: "Raja", age: 25, batch: "LK2014", school: "Hindu College" };
    person.seal(map<string>);

    return person;
}

function sealOpenRecordToMap() returns map<string> {

    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    teacher.seal(map<string>);

    return teacher;
}


