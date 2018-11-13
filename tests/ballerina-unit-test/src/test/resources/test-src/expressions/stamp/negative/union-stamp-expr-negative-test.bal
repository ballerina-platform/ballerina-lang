type Employee record {
    string name;
    string status;
    string batch;
};

//-----------------------Union Type Stamp Negative Test cases --------------------------------------------------

function stampUnionToJSON() returns json {
    int|float|Employee unionVar = { name: "Raja", status: "single", batch: "LK2014", school: "Hindu College" };

    json employee = json.stamp(unionVar);
    return employee;
}

function stampUnionToXML() returns Employee {
    int|float|xml unionVar = xml `<book>The Lost World</book>`;

    Employee employeeValue = Employee.stamp(unionVar);
    return employeeValue;
}

