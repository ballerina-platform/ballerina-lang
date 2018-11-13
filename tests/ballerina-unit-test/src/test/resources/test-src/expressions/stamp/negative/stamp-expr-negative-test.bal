type Employee record {
    string name;
    int age;
    float salary;
};

type Person record {
    string name;
    int age;
};

function stampStreamTypeVariable() returns stream<Person> {

    stream<Employee> employeeStream;
    Employee e1 = { name: "Raja", age: 25, salary: 20000 };
    Employee e2 = { name: "Mohan", age: 45, salary: 10000 };

    stream<Person> personStream = stream<Person>.stamp(employeeStream);
    return personStream;
}

function seaWithInvalidNoOrParameters() returns json {

    json jsonValue = [1, false, null, "foo", { first: "John", last: "Pala" }];
    json returnValue = json.stamp(jsonValue, 34);

    return returnValue;
}

function stampStringValueToJson() returns json {
    string value = "mohan";
    json jsonValue = json.stamp(value);

    return jsonValue;
}

function stampStringValueToAny() returns any {
    string[] stringArray = ["mohan", "mike"];
    any anyValue = any.stamp(stringArray);

    return anyValue;
}

function stampJSONToUnion() returns int|float|json {
    json jsonVar = { name: "Raja", status: "single", batch: "LK2014", school: "Hindu College" };

    int|float|json unionValue = (int|float|json).stamp(jsonVar);
    return unionValue;
}

function stampAnyToString() returns string? {
    any value = "mohan";
    string? stringValue = string.stamp(value);

    return stringValue;
}


function stampJSONArrayToPrimitiveTypeArray() returns int []{

    json intArray = [1, 2, 3, 4];
    int [] returnArray = int[].stamp(intArray);

    return returnArray;
}

function seaWithInvalidTypedesc() returns json {

    json jsonValue = [1, false, null, "foo", { first: "John", last: "Pala" }];
    json returnValue = TestType.stamp(jsonValue);

    return returnValue;
}
