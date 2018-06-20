import ballerina/io;

// Defines a object called 'Person'. It has attached functions both inside and outside of the object.
type Person object {
    public {
        int age,
        string firstName,
        string lastName,
    }

    new(age, firstName, lastName) {
    }

    // Function defined within the object.
    function getFullName() returns string {
        return firstName + " " + lastName;
    }

    // Function declaration defined within the object.
    function checkAndModifyAge(int condition, int a);
};

// Implementation for the declared function.
function Person::checkAndModifyAge(int condition, int a) {
    if (self.age < condition) {
        self.age = a;
    }
}

function main(string... args) {
    // Initializing variable of object type Person
    Person p1 = new(5, "John", "Doe");
    io:println(p1);

    io:println(p1.getFullName());

    p1.checkAndModifyAge(10, 50);

    io:println(p1);
}
