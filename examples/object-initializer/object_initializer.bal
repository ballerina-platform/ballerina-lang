import ballerina/io;

// Defines an object called `Person`. Each object has its own `__init()` method which gets invoked when creating objects. You can place the logic for initializing the fields of the object within the body of the `__init()` method.
type Person object {
    public int age;
    public string name;
    public string fullName;

    private string email = "default@abc.com";
    private int[] marks;

    function __init(int age, string name = "John", string firstname,
        string lastname = "Doe", int... scores) {
        self.age = age;
        self.name = name;
        self.fullName = firstname + " " + lastname;
        self.marks = scores;
    }
};

public function main() {
    // Initializing variable of `object` type `Person`
    Person p1 = new(5, "John", 4, 5);
    io:println(p1);

    Person p2 = new(5, "Adam", name = "Adam", lastname = "Page", 3);
    io:println(p2);
}
