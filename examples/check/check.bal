import ballerina/io;

type Person {
    string name;
    Address? address;
};

type Address {
    string street;
    string city;
};

function getAddress(Person p) returns (Address|error) {
    // if address doesn't exist, return an error.
    match (p.address) {
        Address add => { return add;}
        () => {
            error addressNotFoundErr = { message: "address not found" };
            return addressNotFoundErr;
        }
    }
}

function validateAddress(Person person) returns (boolean|error) {
    //Expression "getAddress(person)!city" produces a value which is union type of string|error.
    //The check operation validates the above expression and If it evaluates to a string, check expression produces a string,
    //If the expression evaluates to an error, check causes to immediately return from the enclosing function with that error, because enclosing function's return type has an error as an alternative.
    string city = check getAddress(person)!city;
    // If check fails, this line won't print.
    io:println(person.name, " has a valid city");
    return true;
}

function validateAddressAgain(Person person) returns boolean {
    //If the expression "getAddress(person)!city" evaluates an error and since enclosing function's return type doesn't have an error as an alternative (only boolean), then Check expression throws the resulted error.
    //Else same as "validateAddress" function behaviour.
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
    // This line won't execute.
    io:println("Tom's result 2:", tomResult2);
}
