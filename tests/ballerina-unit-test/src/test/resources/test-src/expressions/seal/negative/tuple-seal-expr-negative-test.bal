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

type EmployeeObj object {
    public string name;
    public int age;

};

//-----------------------Tuple Type Seal Negative Test cases----------------------------------------------------------

function sealTupleToRecord() returns Employee {
    (string, string, string) tupleValue = ("Mohan", "single", "LK2014");

    Employee returnValue = tupleValue.seal(Employee);
    return returnValue;
}

function sealTupleToJSON() returns json {
    (string, string, string) tupleValue = ("Mohan", "single", "LK2014");

    json jsonValue = tupleValue.seal(json);
    return jsonValue;
}

function sealTupleToXML() returns xml {
    (string, string, string) tupleValue = ("Mohan", "single", "LK2014");

    xml xmlValue = tupleValue.seal(xml);
    return xmlValue;
}

function sealTupleToObject() returns EmployeeObj {
    (string, int) tupleValue = ("Mohan", 30);

    EmployeeObj objectValue = tupleValue.seal(EmployeeObj);
    return objectValue;
}

function sealTupleToMap() returns map {
    (string, string, string) tupleValue = ("Mohan", "single", "LK2014");

    map mapValue = tupleValue.seal(map);
    return mapValue;
}

function sealTupleToArray() returns string[] {
    (string, string, string) tupleValue = ("Mohan", "single", "LK2014");

    string[] arrayValue = tupleValue.seal(string[]);
    return arrayValue;
}

