import ballerina/io;

// Define an abstract object called 'Person'. It should only describe the 
// type of each field and method. Note that an abstract object cannot have
// a constructor method, or any attached functions.
type Person abstract object {
    public int age;
    public string firstName;
    public string lastName;

    // Function declarations can be within the object. But the function cannot 
    // have a body.
    function getFullName() returns string;

    function checkAndModifyAge(int condition, int a);
};

// Define a non-abstract object called 'Employee', which is structurally equivalent
// to 'Person'. Note that a non-abstract object cannot have any member functions 
// without a body.
type Employee object {
    public int age;
    public string firstName;
    public string lastName;

    // Non-abstract object can have a constructor method.
    new(age, firstName, lastName) {
    }

    // Member function should have a body.
    function getFullName() returns string {
        return firstName + " " + lastName; 
    }

    // Otherwise must be defined outside.
    function checkAndModifyAge(int condition, int a);
};

// Implement the declared function.
function Employee::checkAndModifyAge(int condition, int a) {
    if (self.age < condition) {
        self.age = a;
    }
}

public function main() {
    // An abstract object type cannot be initialized. It does not have 
    // an implicit initial value.

    // Initialize a value using the non-abstract object 'Employee',
    // and then assign the value to the abstract object type variable.
    Person p = new Employee(5, "John", "Doe");
    io:println(p.getFullName());

    p.checkAndModifyAge(10, 50);

    io:println(p);
}
