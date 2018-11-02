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

type PersonObj object {
    public int age = 10;
    public string name = "mohan";

    public int year = 2014;
    public string month = "february";
};

type EmployeeObj object {
    public int age = 10;
    public string name = "raj";

};

//-----------------------Union Type Seal -------------------------------------------------------------------

function sealUnionToRecord() returns Employee {
    int|float|Employee unionVar = { name: "Raja", status: "single", batch: "LK2014", school: "Hindu College" };

    unionVar.seal(Employee);
    return unionVar;
}

function sealUnionToJSON() returns json {
    int|float|json unionVar = { name: "Raja", status: "single", batch: "LK2014", school: "Hindu College" };

    unionVar.seal(json);
    return unionVar;
}

function sealUnionToObject() returns EmployeeObj {
    int|float|PersonObj unionVar = new PersonObj();

    unionVar.seal(EmployeeObj);
    return unionVar;
}

function sealUnionToXML() returns xml {
    int|float|xml unionVar = xml `<book>The Lost World</book>`;

    unionVar.seal(xml);
    return unionVar;
}


function sealUnionToIntMap() returns map<int> {
    int|float|map<int> unionVar = { "a": 1, "b": 2 };

    unionVar.seal(map<int>);
    return unionVar;
}

function sealUnionToConstraintMap() returns map<Employee> {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };

    int|float|map<Teacher> unionVar = teacherMap;

    unionVar.seal(map<Employee>);
    return unionVar;
}