import ballerina/io;

// Define an abstract object called `Person`. It should only contain the fields and the
// method declarations.
type Person abstract object {
    public int age;
    public string firstName;
    public string lastName;

    // Method declarations can be within the object. But the method cannot
    // have a body.
    function getFullName() returns string;

};

// Define another abstract object called `Employee`, which references the `Person` object.
type Employee abstract object {
    // Add a reference to the `Person` object type. Note that only abstract objects can be referred to.
    // All the member fields and member methods will be copied from the `Person` object.
    *Person;    
    public float salary;

    function getSalary() returns float;
};

type Owner abstract object {
    public string status;
};

type Manager object {
    // Type references can be chained, by adding a reference to the `Employee` object which
    // again has a reference to the `Employee` object. This will copy all the members from
    // the `Employee` object. It will be same as defining each of those members within this object.
    *Employee;

    // It is possible to have more than one type reference as well.
    *Owner;

    public string dpt;

    // All the fields referenced through the type reference can be accessed within this object.
    function __init(int age, string firstName, string lastName, string status) {
        self.age = age;
        self.firstName = firstName;
        self.lastName = lastName;
        self.status = status;
        self.salary = 2000;
        self.dpt = "HR";
    }

    // Member methods coming from the referenced type can be defined within the object.
    function getFullName() returns string {
        return self.firstName + " " + self.lastName;
    }
};

// Member methods coming from the referenced type can also be defined outside of the object.
function Manager.getSalary() returns float {
        return self.salary;
}

public function main() {
    Manager p = new Manager(5, "John", "Doe", "Senior");

    // Accessing fields that are coming from the referenced type.
    io:println(p.age);
    io:println(p.dpt);

    // Invoking methods that are coming from the referenced type.
    io:println(p.getFullName());
    io:println(p.getSalary());
}
