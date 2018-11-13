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

function stampAnydataToJSON() returns json? {

    anydata anydataValue = 3;
    json? jsonValue = json.stamp(anydataValue);

    return jsonValue;
}

function stampAnydataToRecord() returns Employee? {
    Teacher t1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    anydata anydataValue = t1;
    Employee? employee = Employee.stamp(anydataValue);
    return employee;
}

function stampAnydataToJSONV2() returns json? {
    json t1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    anydata anydataValue = t1;

    json? jsonValue = json.stamp(anydataValue);
    return jsonValue;
}

function stampAnydataToXML() returns xml? {

    anydata anydataValue = xml `<book>The Lost World</book>`;

    xml? xmlValue = xml.stamp(anydataValue);
    return xmlValue;
}

function stampAnydataToMap() returns map<Employee>? {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };
    anydata anydataValue = teacherMap;
    map<Employee>? mapValue = map<Employee>.stamp(anydataValue);

    return mapValue;
}

function stampAnydataToRecordArray() returns Teacher[]? {

    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    Teacher[] teacherArray = [p1, p2];
    anydata anydataValue = teacherArray;
    Teacher[]? returnValue = Teacher[].stamp(anydataValue);

    return returnValue;
}

function stampAnydataToTuple() returns (string,Teacher)? {

    (string, Teacher)  tupleValue = ("Mohan", { name: "Raja", age: 25, status: "single", batch: "LK2014", school:
    "Hindu College" });

    anydata anydataValue = tupleValue;
    (string,Teacher)? returnValue = (string, Teacher).stamp(anydataValue);

    return returnValue;
}

function stampAnydataMapToAnyMap() returns map<anydata> {
    map<anydata> anydataMap = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };

    map<anydata> mapValue = map<anydata>.stamp(anydataMap);
    return mapValue;
}

function stampAnydataToAnydata() returns anydata {
    json jsonValue = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    anydata anydataValue = jsonValue;
    anydata returnValue = anydata.stamp(anydataValue);

    return returnValue;
}