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

    stream<Person> personStream = employeeStream.stamp(stream<Person>);
    return personStream;
}

function seaWithInvalidNoOrParameters() returns json {

    json jsonValue = [1, false, null, "foo", { first: "John", last: "Pala" }];
    json returnValue = jsonValue.stamp(any, 34);

    return returnValue;
}

function stampStringValueToJson() returns json {
    string value = "mohan";
    json jsonValue = value.stamp(json);

    return jsonValue;
}

function stampStringValueToAny() returns any {
    string[] stringArray = ["mohan", "mike"];
    any anyValue = stringArray.stamp(any);

    return anyValue;
}

function stampJSONToUnion() returns int|float|json {
    json jsonVar = { name: "Raja", status: "single", batch: "LK2014", school: "Hindu College" };

    int|float|json unionValue = jsonVar.stamp(int|float|json);
    return unionValue;
}

function stampAnyToString() returns string? {
    any value = "mohan";
    string? stringValue = value.stamp(string);

    return stringValue;
}


function stampJSONArrayToPrimitiveTypeArray() returns int []{

    json intArray = [1, 2, 3, 4];
    int [] returnArray = intArray.stamp(int []);

    return returnArray;
}

function seaWithInvalidTypedesc() returns json {

    json jsonValue = [1, false, null, "foo", { first: "John", last: "Pala" }];
    json returnValue = jsonValue.stamp(TestType);

    return returnValue;
}

//function seaWithInvalidTypedesc() returns any {
//
//    //json jsonValue = [1, false, null, "foo", { first: "John", last: "Pala" }];
//    //any anyValue = "mohan";
//    //json returnValue = jsonValue.stamp(anyValue);
//    any returnValue = getJSON().stamp(any);
//
//    return returnValue;
//}

//function seaWithInvalidTypedescV2() returns json {
//
//    json jsonValue = [1, false, null, "foo", { first: "John", last: "Pala" }];
//    json returnValue = jsonValue.stamp("mohan");
//
//    return returnValue;
//}

function getJSON() returns json {
    json jsonValue = [1, false, null, "foo", { first: "John", last: "Pala" }];
    return jsonValue;

}
