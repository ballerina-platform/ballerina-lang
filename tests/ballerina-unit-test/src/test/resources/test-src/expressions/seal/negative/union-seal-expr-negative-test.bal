type Employee record {
    string name;
    string status;
    string batch;
};

//-----------------------Union Type Seal Negative Test cases --------------------------------------------------

function sealUnionToJSON() returns json {
    int|float|Employee unionVar = { name: "Raja", status: "single", batch: "LK2014", school: "Hindu College" };

    json employee = unionVar.seal(json);
    return employee;
}

function sealUnionToXML() returns Employee {
    int|float|xml unionVar = xml `<book>The Lost World</book>`;

    Employee employeeValue = unionVar.seal(Employee);
    return employeeValue;
}

