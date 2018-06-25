import ballerina/io;

// Defines an object called 'Person'. It contains public and private fields along with their types. Optionally, for fields, the default value can be defined. If not, the default value for that type will automatically get set.
type Person object {
    public {
        int age,
        string name,
        // If you need to have a field with own type, you need to make that field defaultable. Here we have made 'parent' field defaultable by making it nullable.
        Person? parent,
    }
    private {
        string email = "default@abc.com",
    }
};

function main(string... args) {
    // There are three ways to initialize this object.
    Person p1 = new;
    io:println(p1);

    Person p2 = new();
    io:println(p2);

    // This initialization is useful when you need to initialize a different type on the right side.
    Person p3 = new Person();
    io:println(p2);
}
