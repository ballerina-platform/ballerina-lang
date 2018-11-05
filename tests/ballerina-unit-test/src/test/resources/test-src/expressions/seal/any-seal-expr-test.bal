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


function sealAnyToJSON() returns json {

    any anyValue = 3;
    anyValue.seal(json);

    return anyValue;
}

function sealAnyToRecord() returns Employee {
    Teacher t1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    any anyValue = t1;
    anyValue.seal(Employee);
    return anyValue;
}


function sealAnyToJSONV2() returns json {
    json t1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    any anyValue = t1;

    anyValue.seal(json);
    return anyValue;
}

function sealAnyToXML() returns xml {

    any anyValue = xml `<book>The Lost World</book>`;

    anyValue.seal(xml);
    return anyValue;
}

function sealAnyToObject() returns PersonObj {

    any anyValue = new PersonObj();
    anyValue.seal(PersonObj);

    return anyValue;
}

function sealAnyToMap() returns map<Employee> {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };
    any anyValue = teacherMap;
    anyValue.seal(map<Employee>);

    return anyValue;
}

function sealAnyToRecordArray() returns Teacher[] {

    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    Teacher[] teacherArray = [p1, p2];
    any anyValue = teacherArray;
    anyValue.seal(Teacher[]);

    return anyValue;
}

function sealAnyToTuple() returns (string,Teacher) {

    (string, Teacher)  tupleValue = ("Mohan", { name: "Raja", age: 25, status: "single", batch: "LK2014", school:
    "Hindu College" });

    any anyValue = tupleValue;
    anyValue.seal((string, Teacher));

    return anyValue;
}
