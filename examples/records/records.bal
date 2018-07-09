import ballerina/io;

// This defines a new open record named `Person`, with a rest descriptor of `string...`.
type Person record {
    string name,
    int age,
    Person? parent,
    string status,
    // The rest descriptor (a type name followed by ...) restricts the type of arbitrary additional fields that can be added
    // to the record. If a rest descriptor is not specified, `any...` is assumed.
    string...
};

type Department record {
    string name,
    Person[] employees,
    // The `!...` rest descriptor marks the record as a sealed record; i.e., undefined fields are not allowed.
    !...
};

function main(string... args) {
    // This creates an instance of the `Person` record with an empty record literal. The implicit initial value of each
    // field type is assigned to the corresponding field.
    Person p1 = {};
    io:println(p1);

    // This creates an instance of the `Person` record with a non-empty record literal.
    Person p2 = { name: "Jack", age: 10, parent: p1 };
    io:println(p2);

    // This fetches the value of a specific field in this record. Fields can be accessed using dot (.) or the index operator.
    io:println(p2.name);
    io:println(p2["name"]);

    // This fetches the field of a nested record.
    io:println(p2.parent.age);

    // This sets the value of a field.
    p1.name = "Peter";
    p1.age = 35;
    io:println(p1);
    io:println(p2);

    // This adds an additional field not defined in the record type definition above.
    p1.occupation = "Ballerina Developer";
    io:println(p1);

    Person[] emps = [p1];
    // Attempts to add additional fields to a sealed record will result in compile errors.
    Department dpt = {name:"RnD", employees:emps};
    io:println(dpt);
}
