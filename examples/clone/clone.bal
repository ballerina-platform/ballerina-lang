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
    // Define an `Address` record.
    Address address = {
        country : "USA",
        state: "NC",
        city: "Raleigh",
        street: "Daniels St"
    };

    // Define a `Person` record.
    Person person = {
        name: "Alex",
        age: 24,
        married: false,
        salary: 8000.0,
        address: address
    };

    // Assign the `person` value to an `any` typed variable called `anyValue`.
    // The type of the value held by `anyValue` is determined at runtime.
    any anyValue = person;

    // Invoke the `.clone()` built-in method. The return type is either `Person` or `error`. Note that `error` could be
    // returned if the variable on which `.clone()` is called is not of type `anydata`.
    // If the type of the value that is cloned can be determined at compile time and is `anydata`, the type of the
    // return value is exactly the type of the value being cloned.
    // e.g. `Person result = person.clone();`
    // If `person` is cloned instead of `anyValue`, at compile time the compiler will determine that the return type is
    // `Person`.
    var result = anyValue.clone();

    // The type of the value held by an `any` typed variable is only determined at runtime.
    // Therefore, the type of the returned value on a clone attempt could be `error`, in case a non-`anydata` value is
    // found.
    // Check if the `.clone()` attempt is successful, and returns a `Person` value.
    if (result is Person) {
        io:println("Source value: ", person);
        io:println("Cloned value: ", result);
        // Check reference inequality for the original value and the cloned value.
        io:println("Source and Clone are at two different memory locations: ", result !== person);
    // If the result is an error, print out the detailed error message.
    } else if (result is error) {
        io:println("Cannot clone: ", result.detail().message);
    }
}
