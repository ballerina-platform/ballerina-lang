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
    // Defines an `Address` record.
    Address address = {
        country : "USA",
        state: "NC",
        city: "Raleigh",
        street: "Daniels St"
    };

    // Defines a `Person` record.
    Person person = {
        name: "Alex",
        age: 24,
        married: false,
        salary: 8000.0,
        address: address
    };

    // Assigns the `person` value to an `any`-typed variable called `anyValue`.
    // The type of the value held by `anyValue` is determined during runtime.
    any anyValue = person;

    // Invokes the `.clone()` built-in method. The return type is either `Person` or `error`. `error` could be
    // returned if the variable on which `.clone()` is called is not of the type `anydata`.
    // If the type of the value that is cloned can be determined during compile time and is `anydata`, the type of the
    // return value is exactly the type of the value being cloned.
    // E.g., `Person result = person.clone();`
    // If `person` is cloned instead of `anyValue`, during compile time the compiler will determine that the return type is
    // `Person`.
    var result = anyValue.clone();

    // The type of the value held by an `any`-typed variable is only determined during runtime.
    // Therefore, the type of the returned value on a clone attempt could be `error`, in case if a non-`anydata` value is
    // found.
    // Checks if the `.clone()` attempt is successful, and returns a `Person` value.
    if (result is Person) {
        io:println("Source value: ", person);
        io:println("Cloned value: ", result);
        // Checks reference inequality of the original value and the cloned value.
        io:println("Source and Clone are at two different memory locations: ", result !== person);
    // If the result is an error, prints the detailed error message.
    } else if (result is error) {
        io:println("Cannot clone: ", result.detail().message);
    }
}
