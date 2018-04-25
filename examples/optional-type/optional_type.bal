import ballerina/io;

// Now this function optionally returns a string value.
function getValue(string key) returns string|() {
    // In Ballerina, the nil type which is written as `()`, contains a single value, called nil, which is used
    // to represent the absence of any other value.
    // The nil value is written as `()` or `null`.
    // The “null” is syntactic sugar for nil that is to be used with json values as json uses `null` value.
    // The implicit initial value for the nil type is `()`.
    return ();
}

type address {
    string line01;
    string line02;
    string city;
    string state;
    string zipcode;
};

// Here this `addr` and `guardian` fields may or may not contain a value.
type person {
    string name;
    int age;
    address? addr;
    person? guardian;
};

function main(string... args) {
    person p = {};
    io:println(p);

    // Now since the value of the `addr` field is optional, you have to explicitly handle it.
    // The statement `address addr = p.addr` will produce a compilation error.
    // The next example illustrates how you can operate on the address record if a value is available.
    address? addr = p.addr;
    io:println(addr);

    address myAddr = {line01: "61 brandon stree", city: "Santa Clara", state: "CA", zipcode: "95134"};
    p.addr = myAddr;

    addr = p.addr;
    io:println(addr);
}
