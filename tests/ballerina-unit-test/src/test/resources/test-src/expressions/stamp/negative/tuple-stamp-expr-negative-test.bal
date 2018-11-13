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

//-----------------------Tuple Type Stamp Negative Test cases----------------------------------------------------------

function stampTupleToRecord() returns Employee {
    (string, string, string) tupleValue = ("Mohan", "single", "LK2014");

    Employee returnValue = Employee.stamp(tupleValue);
    return returnValue;
}

function stampTupleToJSON() returns json {
    (string, string, string) tupleValue = ("Mohan", "single", "LK2014");

    json jsonValue = json.stamp(tupleValue);
    return jsonValue;
}

function stampTupleToXML() returns xml {
    (string, string, string) tupleValue = ("Mohan", "single", "LK2014");

    xml xmlValue = xml.stamp(tupleValue);
    return xmlValue;
}

function stampTupleToObject() returns EmployeeObj {
    (string, int) tupleValue = ("Mohan", 30);

    EmployeeObj objectValue = EmployeeObj.stamp(tupleValue);
    return objectValue;
}

function stampTupleToMap() returns map {
    (string, string, string) tupleValue = ("Mohan", "single", "LK2014");

    map mapValue = map.stamp(tupleValue);
    return mapValue;
}

function stampTupleToArray() returns string[] {
    (string, string, string) tupleValue = ("Mohan", "single", "LK2014");

    string[] arrayValue = string[].stamp(tupleValue);
    return arrayValue;
}

