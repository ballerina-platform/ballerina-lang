type Employee record {
    string name;
    string status;
    string batch;
};

type Teacher record {
    string name;
    int age;
    string status;
    string batch;
    string school;
};

type PersonObj object {
    public int age = 10;
    public string name = "mohan";

    public int year = 2014;
    public string month = "february";
};

type Student record {
    string name;
    string status;
    string batch;
    string school;
    !...
};

type Person record {
    string name;
    string status;
    string batch;
    string school;
    !...
};


function stampAnyToJSON() returns json? {

    any anyValue = 3;
    json? jsonValue = anyValue.stamp(json);

    return jsonValue;
}

function stampAnyToRecord() returns Employee? {
    Teacher t1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    any anyValue = t1;
    Employee? employee = anyValue.stamp(Employee);
    return employee;
}


function stampAnyToJSONV2() returns json? {
    json t1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    any anyValue = t1;

    json? jsonValue = anyValue.stamp(json);
    return jsonValue;
}

function stampAnyToXML() returns xml? {

    any anyValue = xml `<book>The Lost World</book>`;

    xml? xmlValue = anyValue.stamp(xml);
    return xmlValue;
}

function stampAnyToObject() returns PersonObj? {

    any anyValue = new PersonObj();
    PersonObj? personObj = anyValue.stamp(PersonObj);

    return personObj;
}

function stampAnyToMap() returns map<Employee>? {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };
    any anyValue = teacherMap;
    map<Employee>? mapValue = anyValue.stamp(map<Employee>);

    return mapValue;
}

function stampAnyToRecordArray() returns Teacher[]? {

    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    Teacher[] teacherArray = [p1, p2];
    any anyValue = teacherArray;
    Teacher[]? returnValue = anyValue.stamp(Teacher[]);

    return returnValue;
}

function stampAnyToTuple() returns (string,Teacher)? {

    (string, Teacher)  tupleValue = ("Mohan", { name: "Raja", age: 25, status: "single", batch: "LK2014", school:
    "Hindu College" });

    any anyValue = tupleValue;
    (string,Teacher)? returnValue = anyValue.stamp((string, Teacher));

    return returnValue;
}

function stampAnyToAnydata() returns anydata {
    Teacher t1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    any anyValue = t1;
    anydata anydataValue = anyValue.stamp(anydata);
    return anydataValue;
}

function stampAnytToConstraintJSON() returns json<Person> {

    json<Student> student = { name: "Jon" };
    student.status = "Single";
    student.batch = "LK2014";
    student.school = "Hindu College";

    any anyValue = student;
    json<Person> jsonValue = anyValue.stamp(json<Person>);

    return jsonValue;
}

//------------------------------- Negative Test cases ------------------------------------------------------------

function stampAnyObjectToAnydata() returns anydata {

    any anyValue = new PersonObj();
    anydata anydataValue = anyValue.stamp(anydata);

    return anydataValue;
}

function stampAnyInvalidInput() returns json<Person>  {

    stream<Employee> employeeStream;
    Employee e1 = { name: "Raja", age: 25, salary: 20000 };
    Employee e2 = { name: "Mohan", age: 45, salary: 10000 };

    any anyValue = employeeStream;
    json<Person> jsonValue = anyValue.stamp(json<Person>);

    return jsonValue;
}

