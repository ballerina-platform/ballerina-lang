import ballerina/io;

//Define anydata record
public type Person record {
    string name;
    int age;
    boolean married;
    float salary;
    Address address;
};

public type Address record {
    string country;
    string state;
    string city;
    string street;
};

// The returned tuple will be later used by ballerina tests
public function main() returns (Person, Person, string)? {
    // Create a value of Address
    Address address = {
        country : "USA",
        state: "NC",
        city: "Raleigh",
        street: "Daniels St"
    };

    // Create a value of type Person
    Person person = {
        name: "Alex",
        age: 24,
        married: false,
        salary: 8000.0,
        address: address
    };

    // Note that we have assigned the person value to any typed variable. So the type of the value has to be determined
    // in runtime.
    any anyValue = person;

    // Invoke the clone builtin method. The return type is either Person or error. Note that the error is returned if
    // the value being cloned is not of anydata type. If the type of the value being cloned can be determined in
    // compile time, return value will be exactly of the type of the value being cloned.
    // e.g. Person result = person.clone();
    // In above example, 'person' is cloned instead of 'anyValue'. In compile time itself, we know the return type
    // should be 'Person'
    var result = anyValue.clone();

    // Type of any variable is only determined in runtime. Therefore we have to check if the returned type is Person
    // or an error. If we would have cloned 'person' value, we shouldnt be checking for the type of the return value.
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

     // If the result is an error, prints out the error and return ()
    } else if (result is error) {
        io:println("Cannot clone: ", result.reason());
        return ();
    }
    return ();
}
