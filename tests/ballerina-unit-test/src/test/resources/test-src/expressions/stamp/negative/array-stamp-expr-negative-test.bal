
type Employee record {
    string name;
    string status;
    string batch;
};

type EmployeeObject object {
    string name;
    string status;
    string batch;
};

//----------------------------Array Stamp Negative Test cases -------------------------------------------------------------

function stampAnyArrayToRecord() returns Employee {

    anydata[] anyArray = ["Mohan", "Single", "LK2014"];
    Employee employee = Employee.stamp(anyArray);

    return employee;
}

function stampAnyArrayToXML() returns xml {

    anydata[] anyArray = ["Mohan", "Single", "LK2014"];
    xml xmlValue = xml.stamp(anyArray);

    return xmlValue;
}

function stampAnyArrayToObject() returns EmployeeObject {

    anydata[] anyArray = ["Mohan", "Single", "LK2014"];
    EmployeeObject objectValue = EmployeeObject.stamp(anyArray);

    return objectValue;
}

function stampAnyArrayToMap() returns map {

    anydata[] anyArray = ["Mohan", "Single", "LK2014"];
    map mapValue = map.stamp(anyArray);

    return mapValue;
}

function stampAnyArrayToTuple() returns (string, string, string) {

    anydata[] anyArray = ["Mohan", "Single", "LK2014"];
    (string, string, string) tupleValue = (string, string, string).stamp(anyArray);

    return tupleValue;
}