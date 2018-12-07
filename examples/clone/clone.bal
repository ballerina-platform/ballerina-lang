import ballerina/io;

// `Person` type is defined.
public type Person record {
    string name;
    int age;
    boolean married;
    float salary;
    Address address;
};

// `Address` type is defined.
public type Address record {
    string country;
    string state;
    string city;
    string street;
};

// The returned `tuple` is later used by Ballerina tests.
public function main() returns (Person, Person, string)? {
    // Specify values for the `Address` record.
    Address address = {
        country : "USA",
        state: "NC",
        city: "Raleigh",
        street: "Daniels St"
    };

    // Specify values for the `Person` record.
    Person person = {
        name: "Alex",
        age: 24,
        married: false,
        salary: 8000.0,
        address: address
    };

    // Assign the `person` value to `anyValue`. So the type of the value has to be determined in runtime.
    any anyValue = person;

    // Invoke the clone builtin method. The return type is either `Person` or `error`. Note that the `error` is returned
    // if the value being cloned is not of `anydata` type. If the type of the value being cloned can be determined in
    // compile time, return value is exactly of the type of the value being cloned.
    // e.g. Person result = person.clone();
    // In above example, 'person' is cloned instead of 'anyValue'. In compile time itself, we know the return type
    // should be 'Person'
    var result = anyValue.clone();

    // Type of any variable is only determined in runtime. Therefore we have to check if the returned type is `Person`
    // or an `error`. If we cloned the `person` value, we do not need to check the type of the return value.
    if (result is Person) {
        io:println("Source value: ", person);
        io:println("Cloned value: ", result);
        string refCheck = "";
        // Check if the reference of both values is not the same.
        if (result !== person) {
            refCheck = "Source and Clone are at two different memory locations";
            io:println(refCheck);
        }
        return (person, result, refCheck);

     // If the result is an error, the error is printed out and () is returned.
    } else {
        io:println("Cannot clone: ", result.reason());
        return ();
    }
    return ();
}
