import ballerina/io;

// Defines a class called `Person`. Each class has its own `init()` method, which gets
// invoked when creating the objects. You can place the logic for initializing the fields of the
// class within the body of the `init()` method.
class Person {

    public string name;
    private int age;

    function init(string name, int age) returns error? {
        self.name = name;
        self.age = check validateAge(age);
    }
}

function validateAge(int age) returns int|error {
    if (age > 0 && age < 100) {
        return age;
    }

    error invalidAge = error("The age should be between 0-100");
    return invalidAge;
}

public function main() {
    // Since the `init()` method potentially returns an `error`, the `p1` variable should
    // be of the type `Person|error`.
    Person|error p1 = new("John", 25);
    if (p1 is Person) {
        io:println(p1.name);
    } else {
        io:println(p1.message());
    }

    // `p2` will be an error since the name does not conform to the expected format.
    Person|error p2 = new("Sam", -20);
    if (p2 is Person) {
        io:println(p2.name);
    } else {
        io:println(p2.message());
    }
}
