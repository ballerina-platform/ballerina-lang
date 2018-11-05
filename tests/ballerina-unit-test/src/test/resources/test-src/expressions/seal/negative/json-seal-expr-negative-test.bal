type EmployeeObj object {
    public int age = 10;
    public string name = "raj";

};


function sealJSONToXML() returns xml {

    json jsonValue = { name: "Raja", age: 25, salary: 20000 };

    jsonValue.seal(xml);
    return jsonValue;
}

function sealJSONToObject() returns EmployeeObj {

    json employee = { name: "John", age: 23 };
    employee.seal(EmployeeObj);

    return employee;
}

function sealJSONToTuple() returns (string,string) {

    json jsonValue = { name: "Raja", status: "single"};
    jsonValue.seal((string,string));

    return jsonValue;
}
