import ballerina/io;

// Defines an object called `Person`.
type Person object {
    public int age;
    public string firstName;
    public string lastName;

    // The object initializer.
    function __init(int age, string firstName, string lastName) {
        self.age = age;
        self.firstName = firstName;
        self.lastName = lastName;
    }

    // A method returning the full name value of the `Person` object.
    function getFullName() returns string {
        return self.firstName + " " + self.lastName;
    }

};

public function main() {
    // Initializes a `Person` object.
    Person p1 = new(5, "John", "Doe");

    io:println(p1.getFullName());
}
