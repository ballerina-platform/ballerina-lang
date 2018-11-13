type Employee record {
    string name;
    string status;
    string batch;
};

//-----------------------Union Type Stamp Negative Test cases --------------------------------------------------

function stampUnionToXML() returns Employee {
    int|float|xml unionVar = xml `<book>The Lost World</book>`;

    Employee employeeValue = Employee.stamp(unionVar);
    return employeeValue;
}

