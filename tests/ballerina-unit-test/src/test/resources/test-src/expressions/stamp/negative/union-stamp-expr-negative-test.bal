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

function stampUnionToConstraintMapToUnionNegative() returns int|float|(string, int)|error  {
    int|float|(string, string) unionVar = 2;
    int|float|(string, int)|error  tupleValue = int|float|(string, int).stamp(unionVar);

    return tupleValue;
}

