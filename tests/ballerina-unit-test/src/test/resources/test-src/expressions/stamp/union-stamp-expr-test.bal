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

//-----------------------Union Type Stamp -------------------------------------------------------------------

function stampUnionToRecord() returns Employee {
    int|float|Employee unionVar = { name: "Raja", status: "single", batch: "LK2014", school: "Hindu College" };

    Employee employee = unionVar.stamp(Employee);
    return employee;
}

function stampUnionToJSON() returns json {
    int|float|json unionVar = { name: "Raja", status: "single", batch: "LK2014", school: "Hindu College" };

    json jsonValue = unionVar.stamp(json);
    return jsonValue;
}

function stampUnionToObject() returns EmployeeObj {
    int|float|PersonObj unionVar = new PersonObj();

    EmployeeObj employee = unionVar.stamp(EmployeeObj);
    return employee;
}

function stampUnionToXML() returns xml {
    int|float|xml unionVar = xml `<book>The Lost World</book>`;

    xml xmlValue = unionVar.stamp(xml);
    return xmlValue;
}


function stampUnionToIntMap() returns map<int> {
    int|float|map<int> unionVar = { "a": 1, "b": 2 };

    map<int> mapValue = unionVar.stamp(map<int>);
    return mapValue;
}

function stampUnionToConstraintMap() returns map<Employee> {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };

    int|float|map<Teacher> unionVar = teacherMap;

    map<Employee> mapValue = unionVar.stamp(map<Employee>);
    return mapValue;
}

function stampUnionToAny() returns any {

    int|float|string|boolean unionValue = "mohan";
    any anyValue = unionValue.stamp(any);

    return anyValue;
}

function stampUnionToTuple() returns (string, string) {

    int|float|(string, string) unionVar = ("mohan", "LK2014");
    (string, string) tupleValue = unionVar.stamp((string, string));

    return tupleValue;
}

function stampUnionToAnydata() returns anydata {

    int|float|string|boolean unionValue = "mohan";
    anydata anydataValue = unionValue.stamp(anydata);

    return anydataValue;
}

//-------------------- Negative Test cases ---------------------------------------------------

function stampNegativeUnionToConstraintMap() returns map<Person> {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };

    int|float|map<Teacher> unionVar = teacherMap;

    map<Person> mapValue = unionVar.stamp(map<Person>);
    return mapValue;
}
