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

//----------------------------JSON Seal -------------------------------------------------------------


function sealJSONToAny() returns any {

    json jsonValue = 3;
    any anyValue = jsonValue.seal(any);

    return anyValue;
}

function sealJSONToAnyV2() returns any {

    json jsonValue = [1, false, null, "foo", { first: "John", last: "Pala" }];
    any anyValue = jsonValue.seal(any);

    return anyValue;
}

function sealJSONToRecord() returns Employee {

    json employee = { name: "John", status: "single", batch: "LK2014" };
    Employee employeeValue = employee.seal(Employee);

    return employeeValue;
}

function sealJSONToRecordV2() returns Employee {

    json employee = { name: "John", status: "single", batch: "LK2014", school: "Hindu College" };
    Employee employeeValue = employee.seal(Employee);

    return employeeValue;
}

function sealJSONToJSON() returns json {

    json employee = { name: "John", status: "single", batch: "LK2014", school: "Hindu College" };
    json jsonValue = employee.seal(json);

    return jsonValue;
}

function sealJSONToMap() returns map {

    json employee = { name: "John", status: "single", batch: "LK2014", school: "Hindu College" };
    map mapValue = employee.seal(map);

    return mapValue;
}

function sealJSONToMapV2() returns map {

    json teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College",
        emp : { name: "John", status: "single", batch: "LK2014"} };
    map mapValue = teacher.seal(map);

    return mapValue;
}

function sealConstraintJSONToAny() returns any {

    json<Student> student = { name: "John" };
    student.status = "Single";
    student.batch = "LK2014";
    student.school = "Hindu College";

    any anyValue = student.seal(any);

    return anyValue;
}

function sealConstraintJSONToJSON() returns json {

    json<Student> student = { name: "John" };
    student.status = "Single";
    student.batch = "LK2014";
    student.school = "Hindu College";

    json jsonValue  = student.seal(json);

    return jsonValue;
}

function sealConstraintJSONToConstraintJSON() returns json<Person> {

    json<Student> student = { name: "Jon" };
    student.status = "Single";
    student.batch = "LK2014";
    student.school = "Hindu College";

    json<Person> jsonValue = student.seal(json<Person>);

    return jsonValue;
}

function sealConstraintJSONToConstraintMapV2() returns map {

    json<Student> student = { name: "Jon" };
    student.status = "Single";
    student.batch = "LK2014";
    student.school = "Hindu College";

    map mapValue = student.seal(map);

    return mapValue;
}

function sealJSONArrayToConstraintArray() returns Student []{

    json employeeArray = [{ name: "John", status: "single", batch: "LK2014", school: "Hindu College" },
                            { name: "Raja", status: "married", batch: "LK2014", school: "Hindu College" }];
    Student [] studentArray = employeeArray.seal(Student []);

    return studentArray;
}

function sealJSONArrayToAnyTypeArray() returns any []{

    json jsonArray =  [1, false, "foo", { first: "John", last: "Pala" }];
    any[] anyArray = jsonArray.seal(any []);

    return anyArray;
}

function sealJSONToAnydata() returns anydata {

    json jsonValue = { name: "John", status: "single", batch: "LK2014" };
    anydata anydataValue = jsonValue.seal(anydata);

    return anydataValue;
}

//----------------------------- Negative Test cases ---------------------------------------------------------------

function sealJSONToRecordNegative() returns Student {

    json employee = { name: "John", age : 23, status: "single", batch: "LK2014", school: "Hindu College" };
    Student student = employee.seal(Student);

    return student;

}

function sealJSONToMapNegative() returns map<string> {

    json employee = { name: "John", age : 23, status: "single", batch: "LK2014", school: "Hindu College" };
    map<string> mapValue = employee.seal(map<string>);

    return mapValue;

}
