
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

type Employee record {
    string name;
    int age;
};

//----------------------------Object Seal Negative Test Cases -------------------------------------------------------------


function sealObjectsToRecord() returns Employee {
    PersonObj p = new PersonObj();
    Employee employee = p.seal(Employee);

    return employee;
}


function sealObjectsToJSON() returns json {
    PersonObj p = new PersonObj();
    json jsonValue = p.seal(json);

    return jsonValue;
}

function sealObjectsToXML() returns xml {
    PersonObj p = new PersonObj();
    xml xmlValue = p.seal(xml);

    return xmlValue;
}

function sealObjectsToMap() returns map {
    PersonObj p = new PersonObj();
    map mapValue = p.seal(map);

    return mapValue;
}

function sealObjectsToArray() returns any[] {
    PersonObj p = new PersonObj();
    any[] anyValue = p.seal(any[]);

    return anyValue;
}

function sealObjectsToTuple() returns (int,string) {
    PersonObj p = new PersonObj();
    (int, string) tupleValue = p.seal((int,string));

    return tupleValue;
}
