import ballerina/io;

@Description {value : "Defines a struct called 'Person'. It contains fields and their types. <br/>Optionally, for value type fields, the default value can be defined. If not, the default value is set to the zero-value."}
type Person {
    string name;
    int age = -1;
    Person parent;
    string status;
};

function main (string[] args) {
    //Create an instance of 'Person' using the default values.
    Person p1 = {};
    io:println(p1);

    //Create an instance of the 'Person' struct. Values can be set to any field when initializing. If not set, the fields take their default values.
    Person p2 = {name:"Jack", age:20, parent:p1};
    io:println(p2);

    //Get the value of a specific field in a struct. Fields can be accessed using dot(.) or the index.
    io:println(p2.name);
    io:println(p2["name"]);

    //Get the field of a nested struct.
    io:println(p2.parent.age);

    //Set the value of a field.
    p1.name = "Peter";
    p1.age = 25;
    io:println(p1);
    io:println(p2);
}
