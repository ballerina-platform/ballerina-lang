import ballerina/io;

type Person record {
    string name;
    Address? address;
};

type Address record {
    string street;
    string city;
};

function getAddress(Person p) returns (Address|error) {
    // If the address does not exist, return an error.
    // For type-guard to work, only simple variable references should be used. therefore, `p.address` is assigned to a
    // variable.
    Address? addr = p.address;
    if (addr is Address) {
        return addr;
    } else {
        error addNotFoundErr = error("address not found");
        return addNotFoundErr;
    }
}

function validateAddress(Person person) returns (boolean|error) {
    // The `getAddress(person)!city` expression produces a value which is union type of `string|error`.
    // The `check` operation validates the above expression and if it evaluates to a string, the check expression produces a string.
    // If the expression evaluates to an error, the `check` operation immediately exits the enclosing function with that error.
    // The enclosing function's return type has `error` as an alternative.
    string city = check getAddress(person)!city;
    // If the check fails, this line will not be printed.
    io:println(person.name, " has a valid city");
    return true;
}

public function main() {
    Person bob = { name: "bob", address: () };
    Address address = { street: "1st Avenue", city: "Manhattan" };
    bob.address = address;

    io:println("validating bob...");
    var bobResult1 = validateAddress(bob);
    io:println("Bob's result:", bobResult1);

    Person tom = { name: "tom", address: () };
    io:println("\n", "validating tom...");
    var tomResult1 = validateAddress(tom);
    io:println("Tom's result:", tomResult1);
}
