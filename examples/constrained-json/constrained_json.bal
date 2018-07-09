import ballerina/io;

// Defining a sealed record `Person`.
type Person record {
    string name;
    int age;
    string city;
    !...
};

function main(string... args) {
    // Create a new JSON that is constrained by the Person record. Only the
    // fields defined in the Person record can be accessed. If we try to
    // access a non existing field, it will produce a compilation error.
    json<Person> person = { name: "Jon" };

    // We can access fields defined in the Person record without any issue.
    person.age = 25;
    person.city = "Colombo";
    io:println(person);

    // We can assign this constrained JSON to a JSON. This will allow us to
    // add new elements that are not in the record.
    json employee = person;

    // After that, we can add new elements to the JSON, and access them.
    employee.profession = "Software Engineer";
    io:println(employee.profession);
}
