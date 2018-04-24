import ballerina/io;

@Description {value:"Define a `Person` struct."}
type Person {
    string name;
    int age;
    string city;
};

@Description {value:"Define a transformer to convert the `Person` struct type to a `constrained JSON`."}
transformer <Person p, json<Person> j> updateCity(string city) {
    j.name = p.name;
    j.age = p.age;
    j.city = city;
}


function main (string... args) {
    json j = {"name":"Ann", "age":30, "city":"New York"};

    // Declare a `Person` variable.
    Person p = {};

    // Convert `JSON` to a `Person` type variable.
    var value = <Person>j;

    match value {
        Person pe => p = pe;
        error err => {
        // Print if an error is thrown.
            io:println(err);
        }
    }

    // Define "London' as a constant value for the `city` variable.
    string city = "London";

    // Convert `p `of type `Person` to the response `JSON` using the transformer defined earlier.
    json<Person> response =? <json<Person>, updateCity(city)>p;

    io:println(response);
}

