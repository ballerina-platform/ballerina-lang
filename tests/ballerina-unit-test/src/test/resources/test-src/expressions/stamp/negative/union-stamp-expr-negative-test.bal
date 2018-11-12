type Employee record {
    string name;
    string status;
    string batch;
};

//-----------------------Union Type Stamp Negative Test cases --------------------------------------------------

function stampUnionToJSON() returns json {
    int|float|Employee unionVar = { name: "Raja", status: "single", batch: "LK2014", school: "Hindu College" };

    json employee = unionVar.stamp(json);
    return employee;
}

function stampUnionToXML() returns Employee {
    int|float|xml unionVar = xml `<book>The Lost World</book>`;

    Employee employeeValue = unionVar.stamp(Employee);
    return employeeValue;
}

