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

//----------------------------JSON Stamp -------------------------------------------------------------


function stampJSONToAny() returns any {

    json jsonValue = 3;
    any anyValue = jsonValue.stamp(any);

    return anyValue;
}

function stampJSONToAnyV2() returns any {

    json jsonValue = [1, false, null, "foo", { first: "John", last: "Pala" }];
    any anyValue = jsonValue.stamp(any);

    return anyValue;
}

function stampJSONToRecord() returns Employee {

    json employee = { name: "John", status: "single", batch: "LK2014" };
    Employee employeeValue = employee.stamp(Employee);

    return employeeValue;
}

function stampJSONToRecordV2() returns Employee {

    json employee = { name: "John", status: "single", batch: "LK2014", school: "Hindu College" };
    Employee employeeValue = employee.stamp(Employee);

    return employeeValue;
}

function stampJSONToJSON() returns json {

    json employee = { name: "John", status: "single", batch: "LK2014", school: "Hindu College" };
    json jsonValue = employee.stamp(json);

    return jsonValue;
}

function stampJSONToMap() returns map {

    json employee = { name: "John", status: "single", batch: "LK2014", school: "Hindu College" };
    map mapValue = employee.stamp(map);

    return mapValue;
}

function stampJSONToMapV2() returns map {

    json teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College",
        emp : { name: "John", status: "single", batch: "LK2014"} };
    map mapValue = teacher.stamp(map);

    return mapValue;
}

function stampConstraintJSONToAny() returns any {

    json<Student> student = { name: "John" };
    student.status = "Single";
    student.batch = "LK2014";
    student.school = "Hindu College";

    any anyValue = student.stamp(any);

    return anyValue;
}

function stampConstraintJSONToJSON() returns json {

    json<Student> student = { name: "John" };
    student.status = "Single";
    student.batch = "LK2014";
    student.school = "Hindu College";

    json jsonValue  = student.stamp(json);

    return jsonValue;
}

function stampConstraintJSONToConstraintJSON() returns json<Person> {

    json<Student> student = { name: "Jon" };
    student.status = "Single";
    student.batch = "LK2014";
    student.school = "Hindu College";

    json<Person> jsonValue = student.stamp(json<Person>);

    return jsonValue;
}

function stampConstraintJSONToConstraintMapV2() returns map {

    json<Student> student = { name: "Jon" };
    student.status = "Single";
    student.batch = "LK2014";
    student.school = "Hindu College";

    map mapValue = student.stamp(map);

    return mapValue;
}

function stampJSONArrayToConstraintArray() returns Student []{

    json employeeArray = [{ name: "John", status: "single", batch: "LK2014", school: "Hindu College" },
                            { name: "Raja", status: "married", batch: "LK2014", school: "Hindu College" }];
    Student [] studentArray = employeeArray.stamp(Student []);

    return studentArray;
}

function stampJSONArrayToAnyTypeArray() returns any []{

    json jsonArray =  [1, false, "foo", { first: "John", last: "Pala" }];
    any[] anyArray = jsonArray.stamp(any []);

    return anyArray;
}

function stampJSONToAnydata() returns anydata {

    json jsonValue = { name: "John", status: "single", batch: "LK2014" };
    anydata anydataValue = jsonValue.stamp(anydata);

    return anydataValue;
}

//----------------------------- Negative Test cases ---------------------------------------------------------------

function stampJSONToRecordNegative() returns Student {

    json employee = { name: "John", age : 23, status: "single", batch: "LK2014", school: "Hindu College" };
    Student student = employee.stamp(Student);

    return student;

}

function stampJSONToMapNegative() returns map<string> {

    json employee = { name: "John", age : 23, status: "single", batch: "LK2014", school: "Hindu College" };
    map<string> mapValue = employee.stamp(map<string>);

    return mapValue;

}
