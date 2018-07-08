import ballerina/io;

//This function optionally returns a string value.
function getValue(string key) returns string|() {
    // In Ballerina, the `nil` type that is provided as `()` contains a single value named `nil`. This is used
    // to represent the absence of any other value.
    // The nil value is written as `()` or `null`.
    // `null` is syntactic sugar for `nil` that is to be used with JSON values because JSON uses `null`.
    // The implicit initial value for the `nil` type is `()`.
    return ();
}

type address record {
    string line01;
    string line02;
    string city;
    string state;
    string zipcode;
};

// Here, the `addr` and `guardian` fields may or may not contain values.
type person record {
    string name;
    int age;
    address? addr;
    person? guardian;
};

function main(string... args) {
    person p = {};
    io:println(p);

    // It is optional for the `addr` field to have a value. Therefore, it needs to be handled explicitly.
    // The statement `address addr = p.addr` produces a compilation error.
    // The next example demonstrates how you can operate on the address record if a value is available.
    address? addr = p.addr;
    io:println(addr);

    address myAddr = {line01: "61 brandon stree", city: "Santa Clara", state: "CA", zipcode: "95134"};
    p.addr = myAddr;

    addr = p.addr;
    io:println(addr);
}
