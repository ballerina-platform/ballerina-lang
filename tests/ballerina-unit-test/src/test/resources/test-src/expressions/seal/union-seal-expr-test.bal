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

    Employee employee = unionVar.seal(Employee);
    return employee;
}

function sealUnionToJSON() returns json {
    int|float|json unionVar = { name: "Raja", status: "single", batch: "LK2014", school: "Hindu College" };

    json jsonValue = unionVar.seal(json);
    return jsonValue;
}

function sealUnionToObject() returns EmployeeObj {
    int|float|PersonObj unionVar = new PersonObj();

    EmployeeObj employee = unionVar.seal(EmployeeObj);
    return employee;
}

function sealUnionToXML() returns xml {
    int|float|xml unionVar = xml `<book>The Lost World</book>`;

    xml xmlValue = unionVar.seal(xml);
    return xmlValue;
}


function sealUnionToIntMap() returns map<int> {
    int|float|map<int> unionVar = { "a": 1, "b": 2 };

    map<int> mapValue = unionVar.seal(map<int>);
    return mapValue;
}

function sealUnionToConstraintMap() returns map<Employee> {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };

    int|float|map<Teacher> unionVar = teacherMap;

    map<Employee> mapValue = unionVar.seal(map<Employee>);
    return mapValue;
}

function sealUnionToAny() returns any {

    int|float|string|boolean unionValue = "mohan";
    any anyValue = unionValue.seal(any);

    return anyValue;
}

function sealUnionToTuple() returns (string, string) {

    int|float|(string, string) unionVar = ("mohan", "LK2014");
    (string, string) tupleValue = unionVar.seal((string, string));

    return tupleValue;
}

function sealUnionToAnydata() returns anydata {

    int|float|string|boolean unionValue = "mohan";
    anydata anydataValue = unionValue.seal(anydata);

    return anydataValue;
}

//-------------------- Negative Test cases ---------------------------------------------------

function sealNegativeUnionToConstraintMap() returns map<Person> {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };

    int|float|map<Teacher> unionVar = teacherMap;

    map<Person> mapValue = unionVar.seal(map<Person>);
    return mapValue;
}
