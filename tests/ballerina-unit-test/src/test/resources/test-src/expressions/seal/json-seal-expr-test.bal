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
    jsonValue.seal(any);

    return jsonValue;
}

function sealJSONToAnyV2() returns any {

    json jsonValue = [1, false, null, "foo", { first: "John", last: "Pala" }];
    jsonValue.seal(any);

    return jsonValue;
}

function sealJSONToRecord() returns Employee {

    json employee = { name: "John", status: "single", batch: "LK2014" };
    employee.seal(Employee);

    return employee;
}

function sealJSONToRecordV2() returns Employee {

    json employee = { name: "John", status: "single", batch: "LK2014", school: "Hindu College" };
    employee.seal(Employee);

    return employee;
}

function sealJSONToJSON() returns json {

    json employee = { name: "John", status: "single", batch: "LK2014", school: "Hindu College" };
    employee.seal(json);

    return employee;
}

function sealJSONToMap() returns map {

    json employee = { name: "John", status: "single", batch: "LK2014", school: "Hindu College" };
    employee.seal(map);

    return employee;
}

function sealJSONToMapV2() returns map {

    json teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College",
        emp : { name: "John", status: "single", batch: "LK2014"} };
    teacher.seal(map);

    return teacher;
}

function sealConstraintJSONToAny() returns any {

    json<Student> student = { name: "John" };
    student.status = "Single";
    student.batch = "LK2014";
    student.school = "Hindu College";

    student.seal(any);

    return student;
}

function sealConstraintJSONToJSON() returns json {

    json<Student> student = { name: "John" };
    student.status = "Single";
    student.batch = "LK2014";
    student.school = "Hindu College";

    student.seal(json);

    return student;
}

function sealConstraintJSONToConstraintJSON() returns json<Person> {

    json<Student> student = { name: "Jon" };
    student.status = "Single";
    student.batch = "LK2014";
    student.school = "Hindu College";

    student.seal(json<Person>);

    return student;
}

function sealConstraintJSONToConstraintMapV2() returns map {

    json<Student> student = { name: "Jon" };
    student.status = "Single";
    student.batch = "LK2014";
    student.school = "Hindu College";

    student.seal(map);

    return student;
}

function sealJSONArrayToConstraintArray() returns Student []{

    json employeeArray = [{ name: "John", status: "single", batch: "LK2014", school: "Hindu College" },
                            { name: "Raja", status: "married", batch: "LK2014", school: "Hindu College" }];
    employeeArray.seal(Student []);

    return employeeArray;
}

function sealJSONArrayToPrimitiveTypeArray() returns int []{

    json intArray = [1, 2, 3, 4];
    intArray.seal(int []);

    return intArray;
}

function sealJSONArrayToAnyTypeArray() returns any []{

    json jsonArray =  [1, false, "foo", { first: "John", last: "Pala" }];
    jsonArray.seal(any []);

    return jsonArray;
}

//----------------------------- Negative Test cases ---------------------------------------------------------------

function sealJSONToRecordNegative() returns Student {

    json employee = { name: "John", age : 23, status: "single", batch: "LK2014", school: "Hindu College" };
    employee.seal(Student);

    return employee;

}

function sealJSONToMapNegative() returns map<string> {

    json employee = { name: "John", age : 23, status: "single", batch: "LK2014", school: "Hindu College" };
    employee.seal(map<string>);

    return employee;

}
