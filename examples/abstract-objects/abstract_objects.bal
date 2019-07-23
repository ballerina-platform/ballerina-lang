import ballerina/io;

// Defines an abstract object called `Person`. It should only contain 
// fields and method declarations. An abstract object cannot have
// an initializer or method definitions.
type Person abstract object {
    public int age;
    public string firstName;
    public string lastName;

    // Method declarations can be within the object. However, the method cannot
    // have a body.
    function getFullName() returns string;

    function checkAndModifyAge(int condition, int a);
};

// Defines a non-abstract object called `Employee`, which is structurally equivalent
// to `Person`. A non-abstract object cannot have any methods
// without a body.
type Employee object {
    public int age;
    public string firstName;
    public string lastName;

    // Non-abstract objects can have initializers.
    function __init(int age, string firstName, string lastName) {
        self.age = age;
        self.firstName = firstName;
        self.lastName = lastName;
    }

    // Methods should have a body.
    function getFullName() returns string {
        return self.firstName + " " + self.lastName;
    }

    function checkAndModifyAge(int condition, int a) {
        if (self.age < condition) {
            self.age = a;
        }
    }
};

public function main() {
    // An abstract object type cannot be initialized. It does not have 
    // an implicit initial value.

    // Initializes a value using the non-abstract object `Employee`,
    // and then assigns the value to the abstract object type variable.
    Person p = new Employee(5, "John", "Doe");
    io:println(p.getFullName());
    
    p.checkAndModifyAge(10, 50);

    io:println(p);
}
