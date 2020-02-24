import ballerina/io;
import ballerina/lang.'decimal as decimals;
import ballerina/lang.'float as floats;
import ballerina/lang.'int as ints;

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
    Person|error res = Person.constructFrom(emp);
    if (res is Person) {
        // If the conversion is successful, this prints the `name` field.
        io:println("Employee to Person, name: ", res["name"]);
    } else {
        io:println("Error occurred on conversion");
    }
}

// This function attempts to convert an `anydata` constrained map to an `anydata` record `Person`.
// The conversion would return an error if an incompatible value is found.
function convertAnydataMapToPerson(map<anydata> m) {
    // Attempts to create a new value of the type `Person` from the `map<anydata>` typed `m` value without changing
    // the inherent type of `m`.
    Person|error res = Person.constructFrom(m);
    if (res is Person) {
        // If the conversion is successful, this prints the `name` field.
        io:println("map<anydata> to Person, name: ", res["name"]);
    } else {
        io:println("Error occurred on conversion: ", res.detail());
    }
}

// This function creates numeric values from string values.
function createNumericValues() {
    string s1 = "45";
    string s2 = "abc";
    string s3 = "12.3";
    string s4 = "8";

    // The `fromString()` method in the ballerina/lang.int module returns the integer value represented by a
    // given string if there is a valid representation, else returns an `error`.
    int|error res1 = ints:fromString(s1);
    if (res1 is int) {
        io:println("int value: ", res1);
    } else {
        io:println("error: ", res1.detail());
    }

    res1 = ints:fromString(s2);
    if (res1 is int) {
        io:println("int value: ", res1);
    } else {
        io:println("error: ", res1.detail());
    }

    // The `fromString()` method in the ballerina/lang.float module returns the float value represented by a given
    // string if there is a valid representation, else returns an `error`.
    float|error res2 = floats:fromString(s3);
    if (res2 is float) {
        io:println("float value: ", res2);
    } else {
        io:println("error: ", res2.detail());
    }

    // The `fromString()` method in the ballerina/lang.decimal module returns the decimal value represented by a given
    // string if there is a valid representation, else returns an `error`.
    decimal|error res3 = decimals:fromString(s4);
    if (res3 is decimal) {
        io:println("decimal value: ", res3);
    } else {
        io:println("error: ", res3.detail());
    }
}

public function main() {
    // Attempts to convert an `anydata`-typed record to another `anydata`-typed record.
    Employee emp = {name: "Jack Sparrow", age: 54, empNo: 100};
    convertEmployeeToPerson(emp);

    // Attempts to convert an `anydata` constrained map to an `anydata`-typed record.
    // This conversion would be successful since all the expected elements are present.
    map<anydata> m = {name: "Hector Barbossa", age: 54, empNo: 100};
    convertAnydataMapToPerson(m);

    // This conversion would not be successful since all the required elements are not
    // present in the map.
    map<anydata> n = {name: "Elizabeth Swann"};
    convertAnydataMapToPerson(n);

    // Attempts to convert strings to numeric types.
    createNumericValues();
}
