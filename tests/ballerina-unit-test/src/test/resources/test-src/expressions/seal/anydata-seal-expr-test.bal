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


function sealAnydataToJSON() returns json? {

    anydata anydataValue = 3;
    json? jsonValue = anydataValue.seal(json);

    return jsonValue;
}

function sealAnydataToRecord() returns Employee? {
    Teacher t1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    anydata anydataValue = t1;
    Employee? employee = anydataValue.seal(Employee);
    return employee;
}

function sealAnydataToJSONV2() returns json? {
    json t1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    anydata anydataValue = t1;

    json? jsonValue = anydataValue.seal(json);
    return jsonValue;
}

function sealAnydataToXML() returns xml? {

    anydata anydataValue = xml `<book>The Lost World</book>`;

    xml? xmlValue = anydataValue.seal(xml);
    return xmlValue;
}

//function sealAnyToObject() returns PersonObj? {
//
//    anydata anyValue = new PersonObj();
//    PersonObj? personObj = anyValue.seal(PersonObj);
//
//    return personObj;
//}

function sealAnydataToMap() returns map<Employee>? {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };
    anydata anydataValue = teacherMap;
    map<Employee>? mapValue = anydataValue.seal(map<Employee>);

    return mapValue;
}

function sealAnydataToRecordArray() returns Teacher[]? {

    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    Teacher[] teacherArray = [p1, p2];
    anydata anydataValue = teacherArray;
    Teacher[]? returnValue = anydataValue.seal(Teacher[]);

    return returnValue;
}

function sealAnydataToTuple() returns (string,Teacher)? {

    (string, Teacher)  tupleValue = ("Mohan", { name: "Raja", age: 25, status: "single", batch: "LK2014", school:
    "Hindu College" });

    anydata anydataValue = tupleValue;
    (string,Teacher)? returnValue = anydataValue.seal((string, Teacher));

    return returnValue;
}
