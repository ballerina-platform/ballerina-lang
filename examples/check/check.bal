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

function validateAddressAgain(Person person) returns boolean {
    //The enclosing function's return type does not include `error` as an alternative (it allows only boolean).
    //So, if the "getAddress(person)!city" expression evaluates an error, 
    //the `check` expression panics the resulted error.
    //Else, the function behaviour is the same as the `validateAddress` function.
    string|error city = getAddress(person)!city;
    if (city is error) {
        panic city;
    }
    // If check fails, this line won't print.
    io:println(person.name, " has a valid city");
    return true;
}

public function main() {
    Person bob = { name: "bob", address: () };
    Address address = { street: "1st Avenue", city: "Manhattan" };
    bob.address = address;

    io:println("validating bob...");
    var bobResult1 = validateAddress(bob);
    io:println("Bob's result 1:", bobResult1);
    boolean bobResult2 = validateAddressAgain(bob);
    io:println("Bob's result 2:", bobResult2);

    Person tom = { name: "tom", address: () };
    io:println("\n", "validating tom...");
    var tomResult1 = validateAddress(tom);
    io:println("Tom's result 1:", tomResult1);
    var tomResult2 = validateAddressAgain(tom);
    // This line will not be executed.
    io:println("Tom's result 2:", tomResult2);
}
