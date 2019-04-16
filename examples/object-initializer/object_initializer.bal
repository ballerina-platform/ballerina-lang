import ballerina/io;

// Defines an object called `Person`. Each object has its own `__init()` method which gets
// invoked when creating objects. You can place the logic for initializing the fields of the
// object within the body of the `__init()` method.
type Person object {

    public string name;
    private int age;

    function __init(string name, int age) returns error? {
        self.name = check validateName(name);
        self.age = age;
    }
};

function validateName(string name) returns string|error {
    if (check name.matches("[A-Za-z]*")) {
        return name;
    }

    error invName = error("Invalid name format");
    return invName;
}

public function main() {
    // Since the `__init()` method potentially returns an `error`, the `p1` variable should
    // be of type `Person|error`.
    Person|error p1 = new("John", 25);
    io:println(p1);

    // `p2` will be an error since the name does not conform to the expected format.
    Person|error p2 = new("John123", 25);
    io:println(p2);
}
