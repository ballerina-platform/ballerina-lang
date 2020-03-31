import ballerina/io;

// A record representing a `Person`.
public type Person record {
    string name;
    int age;
    boolean married;
    float salary;
    Address address;
};

// A record representing an `Address`.
public type Address record {
    string country;
    string state;
    string city;
    string street;
};

public function main() {
    // Define an `Address` value.
    Address address = {
        country: "USA",
        state: "NC",
        city: "Raleigh",
        street: "Daniels St"
    };

    // Define a `Person` value.
    Person person = {
        name: "Alex",
        age: 24,
        married: false,
        salary: 8000.0,
        address: address
    };

    // Invoke the `.clone()` method. The return type is also `Person`.
    // Provided that the type of the value being cloned belongs to `anydata`, the return type of `.clone()` would be
    // the same as the type of the value being cloned.
    // E.g., `anydata result = anydataValue.clone();` where `anydataValue` is a variable of type `anydata`.
    Person result = person.clone();

    io:println("Source value: ", person);
    io:println("Cloned value: ", result);
    // Check reference inequality between the original value and the cloned value.
    io:println("Source and Clone are at two different memory locations: ", result !== person);
}
