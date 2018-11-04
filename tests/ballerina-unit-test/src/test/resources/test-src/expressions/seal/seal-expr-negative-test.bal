type Employee record {
    string name;
    int age;
    float salary;
};

type Person record {
    string name;
    int age;
};

function sealStreamTypeVariable() returns stream<Person> {

    stream<Employee> employeeStream;
    Employee e1 = { name: "Raja", age: 25, salary: 20000 };
    Employee e2 = { name: "Mohan", age: 45, salary: 10000 };

    employeeStream.seal(stream<Person>);
}

function seaWithInvalidNoOrParameters() returns json {

    json jsonValue = [1, false, null, "foo", { first: "John", last: "Pala" }];
    jsonValue.seal(any, 34);

    return jsonValue;
}

function sealStringValueToJson() returns json {
    string value = "mohan";
    value.seal(json);

    return value;
}

function sealStringValueToAny() returns any {
    string[] stringArray = ["mohan", "mike"];
    stringArray.seal(any);

    return stringArray;
}

function sealJSONToUnion() returns int|float|json {
    json jsonVar = { name: "Raja", status: "single", batch: "LK2014", school: "Hindu College" };

    jsonVar.seal(int|float|json);
    return jsonVar;
}
