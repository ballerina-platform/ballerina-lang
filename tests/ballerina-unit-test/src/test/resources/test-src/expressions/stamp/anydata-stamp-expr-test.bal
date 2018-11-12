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
    json? jsonValue = anydataValue.stamp(json);

    return jsonValue;
}

function stampAnydataToRecord() returns Employee? {
    Teacher t1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    anydata anydataValue = t1;
    Employee? employee = anydataValue.stamp(Employee);
    return employee;
}

function stampAnydataToJSONV2() returns json? {
    json t1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    anydata anydataValue = t1;

    json? jsonValue = anydataValue.stamp(json);
    return jsonValue;
}

function stampAnydataToXML() returns xml? {

    anydata anydataValue = xml `<book>The Lost World</book>`;

    xml? xmlValue = anydataValue.stamp(xml);
    return xmlValue;
}

function stampAnydataToMap() returns map<Employee>? {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };
    anydata anydataValue = teacherMap;
    map<Employee>? mapValue = anydataValue.stamp(map<Employee>);

    return mapValue;
}

function stampAnydataToRecordArray() returns Teacher[]? {

    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    Teacher[] teacherArray = [p1, p2];
    anydata anydataValue = teacherArray;
    Teacher[]? returnValue = anydataValue.stamp(Teacher[]);

    return returnValue;
}

function stampAnydataToTuple() returns (string,Teacher)? {

    (string, Teacher)  tupleValue = ("Mohan", { name: "Raja", age: 25, status: "single", batch: "LK2014", school:
    "Hindu College" });

    anydata anydataValue = tupleValue;
    (string,Teacher)? returnValue = anydataValue.stamp((string, Teacher));

    return returnValue;
}

function stampAnydataMapToAny() returns any {
    map<anydata> anydataMap = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };

    any anyValue = anydataMap.stamp(any);
    return anyValue;
}

function stampAnydataMapToAnyMap() returns map {
    map<anydata> anydataMap = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };

    map mapValue = anydataMap.stamp(map);
    return mapValue;
}

function stampAnydataToAnydata() returns anydata {
    json jsonValue = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    anydata anydataValue = jsonValue;
    anydata returnValue = anydataValue.stamp(anydata);

    return returnValue;
}