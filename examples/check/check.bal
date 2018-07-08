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
    match (p.address) {
        Address add => { return add;}
        () => {
            error addNotFoundErr = { message: "address not found" };
            return addNotFoundErr;
        }
    }
}

function validateAddress(Person person) returns (boolean|error) {
    // The `getAddress(person)!city` expression produces a value which is union type of `string|error`.
    // The `check` operation validates the above expression and if it evaluates to a string, the check expression produces a string.
    // If the expression evaluates to an error, the `check` operation immediately exits the enlosing function with that error.
    // The enclosing function's return type has `error` as an alternative.
    string city = check getAddress(person)!city;
    // If the check fails, this line will not be printed.
    io:println(person.name, " has a valid city");
    return true;
}

function validateAddressAgain(Person person) returns boolean {
    //The enclosing function's return type does not include `error` as an alternative (it allows only boolean).
    //So, if the "getAddress(person)!city" expression evaluates an error, 
    //the `check` expression throws the resulted error.
    //Else, the function behaviour is the same as the `validateAddress` function.
    string city = check getAddress(person)!city;
    // If check fails, this line won't print.
    io:println(person.name, " has a valid city");
    return true;
}

function main(string... args) {
    Person bob = { name: "bob" };
    Address address = { street: "1st Avenue", city: "Manhattan" };
    bob.address = address;

    io:println("validating bob...");
    var bobResult1 = validateAddress(bob);
    io:println("Bob's result 1:", bobResult1);
    boolean bobResult2 = validateAddressAgain(bob);
    io:println("Bob's result 2:", bobResult2);

    Person tom = { name: "tom" };
    io:println("\n", "validating tom...");
    var tomResult1 = validateAddress(tom);
    io:println("Tom's result 1:", tomResult1);
    var tomResult2 = validateAddressAgain(tom);
    // This line will not be executed.
    io:println("Tom's result 2:", tomResult2);
}
