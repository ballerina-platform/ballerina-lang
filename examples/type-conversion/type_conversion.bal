import ballerina/io;

type Person record {
    string name;
    int age;
};

type Employee record {
    string name;
    int age;
    int empNo;
};

// This function attempts to convert an `anydata` record `Employee` to an `anydata` record `Person`.
function convertEmployeeToPerson(Employee emp) {
    // Attempts to create a new value of the type `Person` from the `Employee`-typed `emp` value without changing 
    // the inherent type of `emp`.
    Person|error res = Person.convert(emp);
    if (res is Person) {
        // If the conversion is successful, this prints the `name` field.
        io:println("Employee to Person, name: ", res["name"]);
    } else {
        io:println("Error occurred on conversion");
    }
}

// This function attempts to convert an `any` constrained map to an `anydata` record `Person`.
// The conversion would return an error if non-`anydata` or an incompatible value is found.
function convertAnyMapToPerson(map<any> m) {
    // Attempts to create a new value of the type `Person` from the `map<any>` typed `m` value without changing
    // the inherent type of `m`.
    Person|error res = Person.convert(m);
    if (res is Person) {
        // If the conversion is successful, this prints the `name` field.
        io:println("map<any> to Person, name: ", res["name"]);
    } else {
        io:println("Error occurred on conversion");
    }
}

// This function converts simple basic types using the `.convert()` built-in method.
function convertSimpleBasicTypes() {
    string s1 = "45";
    string s2 = "abc";
    string s3 = "true";
    float f = 10.2;
    any a = 3.14;

    // The `string` to `int` conversion is unsafe since the `string` value may not be convertible to `int`.
    int|error res1 = int.convert(s1);
    if (res1 is int) {
        io:println("int value: ", res1);
    } else {
        io:println("error: ", res1.detail().message);
    }

    res1 = int.convert(s2);
    if (res1 is int) {
        io:println("int value: ", res1);
    } else {
        io:println("error: ", res1.detail().message);
    }

    // A `float` to `int` conversion can result in some of the information getting lost.
    // However, this conversion is unsafe due to `NaN` or `infinite` float values in which the conversion attempt will 
    // result in an error.
    int|error intVal = int.convert(f);
    if (intVal is int) {
        io:println("int value: ", intVal);
    }
    // A `string` to `boolean` conversion is always safe. The `string` value `true` (ignoring case)
    // evaluates to the `boolean` value `true` while any other `string` is converted to the
    // `boolean` value `false`.
    boolean b = boolean.convert(s3);
    io:println("boolean value: ", b);

    // A simple basic-typed value held in an `any`-typed variable can also be converted to its inherent
    // type using the `.convert()` method. This attempt is unsafe since the value may not be compatible
    // with the target type.
    float|error res2 = float.convert(a);
    if (res2 is float) {
        io:println("float value: ", res2);
    } else {
        io:println("error: ", res2.detail().message);
    }
}

public function main() {
    // Attempts to convert an `anydata`-typed record to another `anydata`-typed record.
    Employee emp = { name: "Jack Sparrow", age: 54, empNo: 100 };
    convertEmployeeToPerson(emp);

    // Attempts to convert an `any` constrained map to an `anydata`-typed record.
    // This conversion would be successful since all the expected elements are present
    // and no non-`anydata` elements are added to the map.
    map<any> m = { name: "Jack Sparrow", age: 54, empNo: 100 };
    convertAnyMapToPerson(m);

    // Adds a non-`anydata` element (e.g., `typedesc`) to the map and re-attempts on the conversion.
    // The conversion attempt would now return an error.
    m["name"] = int;
    convertAnyMapToPerson(m);

    // Attempts to convert the to/from simple basic types.
    convertSimpleBasicTypes();
}
