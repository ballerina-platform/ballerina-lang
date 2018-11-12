type EmployeeObj object {
    public int age = 10;
    public string name = "raj";

};


function stampJSONToXML() returns xml {

    json jsonValue = { name: "Raja", age: 25, salary: 20000 };

    xml xmlValue = jsonValue.stamp(xml);
    return xmlValue;
}

function stampJSONToObject() returns EmployeeObj {

    json employee = { name: "John", age: 23 };
    EmployeeObj employeeObj = employee.stamp(EmployeeObj);

    return employeeObj;
}

function stampJSONToTuple() returns (string, string) {

    json jsonValue = { name: "Raja", status: "single" };
    (string, string) tupleValue = jsonValue.stamp((string, string));

    return tupleValue;
}
