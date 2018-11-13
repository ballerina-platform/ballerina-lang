
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
    Employee employee = Employee.stamp(p);

    return employee;
}


function stampObjectsToJSON() returns json {
    PersonObj p = new PersonObj();
    json jsonValue = json.stamp(p);

    return jsonValue;
}

function stampObjectsToXML() returns xml {
    PersonObj p = new PersonObj();
    xml xmlValue = xml.stamp(p);

    return xmlValue;
}

function stampObjectsToMap() returns map {
    PersonObj p = new PersonObj();
    map mapValue = map.stamp(p);

    return mapValue;
}

function stampObjectsToArray() returns any[] {
    PersonObj p = new PersonObj();
    any[] anyValue = any[].stamp(p);

    return anyValue;
}

function stampObjectsToTuple() returns (int,string) {
    PersonObj p = new PersonObj();
    (int, string) tupleValue = (int,string).stamp(p);

    return tupleValue;
}
