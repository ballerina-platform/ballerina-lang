
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

    any[] anyArray = ["Mohan", "Single", "LK2014"];
    Employee employee = anyArray.stamp(Employee);

    return employee;
}

function stampAnyArrayToXML() returns xml {

    any[] anyArray = ["Mohan", "Single", "LK2014"];
    xml xmlValue = anyArray.stamp(xml);

    return xmlValue;
}

function stampAnyArrayToObject() returns EmployeeObject {

    any[] anyArray = ["Mohan", "Single", "LK2014"];
    EmployeeObject objectValue = anyArray.stamp(EmployeeObject);

    return objectValue;
}

function stampAnyArrayToMap() returns map {

    any[] anyArray = ["Mohan", "Single", "LK2014"];
    map mapValue = anyArray.stamp(map);

    return mapValue;
}

function stampAnyArrayToTuple() returns (string, string, string) {

    any[] anyArray = ["Mohan", "Single", "LK2014"];
    (string, string, string) tupleValue = anyArray.stamp((string, string, string));

    return tupleValue;
}