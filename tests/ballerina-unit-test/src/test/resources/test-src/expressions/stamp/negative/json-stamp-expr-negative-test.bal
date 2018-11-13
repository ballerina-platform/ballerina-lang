type EmployeeObj object {
    public int age = 10;
    public string name = "raj";

};


function stampJSONToXML() returns xml {

    json jsonValue = { name: "Raja", age: 25, salary: 20000 };

    xml xmlValue = xml.stamp(jsonValue);
    return xmlValue;
}

function stampJSONToObject() returns EmployeeObj {

    json employee = { name: "John", age: 23 };
    EmployeeObj employeeObj = EmployeeObj.stamp(employee);

    return employeeObj;
}

function stampJSONToTuple() returns (string, string) {

    json jsonValue = { name: "Raja", status: "single" };
    (string, string) tupleValue = (string, string).stamp(jsonValue);

    return tupleValue;
}
