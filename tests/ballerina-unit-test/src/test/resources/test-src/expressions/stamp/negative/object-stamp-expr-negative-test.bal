
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

//----------------------------Object Stamp Negative Test Cases -------------------------------------------------------------


function stampObjectsToRecord() returns Employee {
    PersonObj p = new PersonObj();
    Employee employee = p.stamp(Employee);

    return employee;
}


function stampObjectsToJSON() returns json {
    PersonObj p = new PersonObj();
    json jsonValue = p.stamp(json);

    return jsonValue;
}

function stampObjectsToXML() returns xml {
    PersonObj p = new PersonObj();
    xml xmlValue = p.stamp(xml);

    return xmlValue;
}

function stampObjectsToMap() returns map {
    PersonObj p = new PersonObj();
    map mapValue = p.stamp(map);

    return mapValue;
}

function stampObjectsToArray() returns any[] {
    PersonObj p = new PersonObj();
    any[] anyValue = p.stamp(any[]);

    return anyValue;
}

function stampObjectsToTuple() returns (int,string) {
    PersonObj p = new PersonObj();
    (int, string) tupleValue = p.stamp((int,string));

    return tupleValue;
}
