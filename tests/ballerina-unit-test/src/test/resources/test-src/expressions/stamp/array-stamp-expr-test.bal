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

//----------------------------Array Stamp -------------------------------------------------------------


function stampRecordToAnyArray() returns any[] {

    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    Teacher[] teacherArray = [p1, p2];
    any[] anyArray = teacherArray.stamp(any[]);

    return anyArray;
}

function stampAnyToRecordArray() returns Teacher[] {

    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    any[] anyArray = [p1, p2];
    Teacher[] teacherArray = anyArray.stamp(Teacher[]);

    return teacherArray;
}

function stampAnyToSimilarOpenRecordArray() returns Employee[] {

    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    any[] teacherArray = [p1, p2];
    Employee[] employeeArray = teacherArray.stamp(Employee[]);

    return employeeArray;
}

function stampRecordToSimilarOpenRecordArray() returns Employee[] {

    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    Teacher[] teacherArray = [p1, p2];
    Employee[] employeeArray = teacherArray.stamp(Employee[]);

    return employeeArray;
}

function stampConstraintArrayToJSONArray() returns json{

    Student [] studentArray = [{ name: "John", status: "single", batch: "LK2014", school: "Hindu College" },
    { name: "Raja", status: "married", batch: "LK2014", school: "Hindu College" }];

    json jsonArray = studentArray.stamp(json);

    return jsonArray;
}

function stampRecordToAnydata() returns anydata {

    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    Teacher[] teacherArray = [p1, p2];
    anydata anydataArray = teacherArray.stamp(anydata);

    return anydataArray;
}

function stampRecordToAnydataArray() returns anydata[] {

    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    Teacher[] teacherArray = [p1, p2];
    anydata[] anydataArray = teacherArray.stamp(anydata[]);

    return anydataArray;
}

