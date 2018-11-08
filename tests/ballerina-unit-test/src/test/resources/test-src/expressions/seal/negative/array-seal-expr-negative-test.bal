
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

//----------------------------Array Seal Negative Test cases -------------------------------------------------------------

function sealAnyArrayToRecord() returns Employee {

    any[] anyArray = ["Mohan", "Single", "LK2014"];
    Employee employee = anyArray.seal(Employee);

    return employee;
}

function sealAnyArrayToXML() returns xml {

    any[] anyArray = ["Mohan", "Single", "LK2014"];
    xml xmlValue = anyArray.seal(xml);

    return xmlValue;
}

function sealAnyArrayToObject() returns EmployeeObject {

    any[] anyArray = ["Mohan", "Single", "LK2014"];
    EmployeeObject objectValue = anyArray.seal(EmployeeObject);

    return objectValue;
}

function sealAnyArrayToMap() returns map {

    any[] anyArray = ["Mohan", "Single", "LK2014"];
    map mapValue = anyArray.seal(map);

    return mapValue;
}

function sealAnyArrayToTuple() returns (string, string, string) {

    any[] anyArray = ["Mohan", "Single", "LK2014"];
    (string, string, string) tupleValue = anyArray.seal((string, string, string));

    return tupleValue;
}