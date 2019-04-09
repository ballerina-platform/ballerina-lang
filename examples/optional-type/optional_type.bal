import ballerina/io;

//This function optionally returns a `string` value. Here, `string?` is equivalent to `string|()`.
function getValue(string key) returns string? {
    // In Ballerina, the `nil` type that is provided as `()` contains a single value named `nil`. This is used
    // to represent the absence of any other value.
    // The `nil` value is written as `()`.
    // `null` is syntactic sugar for `nil` that is to be used with JSON values because JSON uses `null`.
    return ();
}

type Address record {
    string line01;
    string line02;
    string city;
    string state;
    string zipcode;
};

// Here, the `addr` and `guardian` fields may or may not contain values. And either all the fields must be assigned with
// default values e.g. `string name = ""` or be initialized before use.
type Person record {
    string name;
    int age;
    Address? addr;
    Person? guardian;
};

public function main() {
    Person p = { name: "Paul", age: 40, addr: (), guardian:() };
    io:println(p);

    // It is optional for the `addr` field to have a value. Therefore, it needs to be handled explicitly.
    // The statement `Address addr = p.addr` produces a compilation error.
    // The next example demonstrates how you can operate on the `Address` record if a value is available.
    Address? addr = p.addr;
    io:println(addr);

    Address myAddr = {
        line01: "No. 61",
        line02: "Brandon street",
        city: "Santa Clara",
        state: "CA",
        zipcode: "95134"
    };
    p.addr = myAddr;

    addr = p.addr;
    io:println(addr);
}
