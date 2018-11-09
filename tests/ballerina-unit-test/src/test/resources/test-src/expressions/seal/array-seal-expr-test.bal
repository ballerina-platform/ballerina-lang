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

type Teacher record {
    string name;
    int age;
    string status;
    string batch;
    string school;
};

//----------------------------Array Seal -------------------------------------------------------------


function sealRecordToAnyArray() returns any[] {

    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    Teacher[] teacherArray = [p1, p2];
    any[] anyArray = teacherArray.seal(any[]);

    return anyArray;
}

function sealAnyToRecordArray() returns Teacher[] {

    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    any[] anyArray = [p1, p2];
    Teacher[] teacherArray = anyArray.seal(Teacher[]);

    return teacherArray;
}

function sealAnyToSimilarOpenRecordArray() returns Employee[] {

    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    any[] teacherArray = [p1, p2];
    Employee[] employeeArray = teacherArray.seal(Employee[]);

    return employeeArray;
}

function sealRecordToSimilarOpenRecordArray() returns Employee[] {

    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    Teacher[] teacherArray = [p1, p2];
    Employee[] employeeArray = teacherArray.seal(Employee[]);

    return employeeArray;
}

function sealConstraintArrayToJSONArray() returns json{

    Student [] studentArray = [{ name: "John", status: "single", batch: "LK2014", school: "Hindu College" },
    { name: "Raja", status: "married", batch: "LK2014", school: "Hindu College" }];

    json jsonArray = studentArray.seal(json);

    return jsonArray;
}

function sealRecordToAnydata() returns anydata {

    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    Teacher[] teacherArray = [p1, p2];
    anydata anydataArray = teacherArray.seal(anydata);

    return anydataArray;
}

function sealRecordToAnydataArray() returns anydata[] {

    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    Teacher[] teacherArray = [p1, p2];
    anydata[] anydataArray = teacherArray.seal(anydata[]);

    return anydataArray;
}

