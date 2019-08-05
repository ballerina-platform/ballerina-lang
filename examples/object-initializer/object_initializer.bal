import ballerina/io;

// Defines an object called `Person`. Each object has its own `__init()` method, which gets
// invoked when creating the objects. You can place the logic for initializing the fields of the
// object within the body of the `__init()` method.
type Person object {

    public string name;
    private int age;

    function __init(string name, int age) returns error? {
        self.name = name;
        self.age = check validateAge(age);
    }
};

function validateAge(int age) returns int|error {
    if (age > 0 && age < 100) {
        return age;
    }

    error invalidAge = error("The age should be between 0-100");
    return invalidAge;
}

public function main() {
    // Since the `__init()` method potentially returns an `error`, the `p1` variable should
    // be of the type `Person|error`.
    Person|error p1 = new("John", 25);
    io:println(p1);

    // `p2` will be an error since the name does not conform to the expected format.
    Person|error p2 = new("Sam", -20);
    io:println(p2);
}
