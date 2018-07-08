import ballerina/io;

// This defines a new record named `Person`.
type Person record {
    string name;
    int age;
    Person? parent;
    string status;
};

function main(string... args) {
    // This creates an instance of the`Person` record with an empty record literal. The implicit initial value of each
    // field type is assigned to the corresponding field.
    Person p1 = {};
    io:println(p1);

    // This creates an instance of the `Person` record with a non-empty record literal.
    Person p2 = { name: "Jack", age: 20, parent: p1 };
    io:println(p2);

    // This fetches the value of a specific field in this record. Fields can be accessed using dot (.) or the index operator.
    io:println(p2.name);
    io:println(p2["name"]);

    // This fetches the field of a nested struct.
    io:println(p2.parent.age);

    // This sets the value of a field.
    p1.name = "Peter";
    p1.age = 25;
    io:println(p1);
    io:println(p2);
}
