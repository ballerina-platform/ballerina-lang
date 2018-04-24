import ballerina/io;

// Here is how you can define a new record called 'Person'.
type Person {
    string name;
    int age;
    Person? parent;
    string status;
};

function main(string... args) {
    // Here we create an instance of 'Person' record with an empty record literal. Implicit initial value of each
    // field type will be assigned to the corresponding field.
    Person p1 = {};
    io:println(p1);

    // Here we create an instance of the `person` record with a non-empty record literal.
    Person p2 = { name: "Jack", age: 20, parent: p1 };
    io:println(p2);

    // Get the value of a specific field in this record. Fields can be accessed using dot(.) or the index operator.
    io:println(p2.name);
    io:println(p2["name"]);

    // Get the field of a nested struct.
    io:println(p2.parent.age);

    // Set the value of a field.
    p1.name = "Peter";
    p1.age = 25;
    io:println(p1);
    io:println(p2);
}
