import ballerina/io;

// Define an abstract object called 'Person'. It should only describe the 
// type of each field and method.
type Person abstract object {
    public int age;
    public string firstName;
    public string lastName;

    // Function declarations can be within the object. But the function cannot 
    // have a body.
    function getFullName() returns string;

};

// Define another abstract object called 'Employee', which references the 'Person' object.
type Employee abstract object {
    // Add a reference to the 'Person'. Note that only abstract objects can be referred.
    // All the member fields and member methods will be copied from the 'Person' object.
    *Person;    
    public float salary;

    function getSalary() returns float;
};

type Owner abstract object {
    public string status;
};

type Manager object {
    // Type references can be chained, by adding a reference to the 'Employee' object which 
    // again has a reference to the 'Employee' object. This will copy all the members from 
    // the 'Employee' object. It will be same as those were defined within this 'Manager' object.
    *Employee;

    // It is possible to have more than one type references as well.
    *Owner;

    public string dpt;

    // All the fileds referenced through th type reference can be accessed within this object.
    new(age, firstName, lastName) {
        salary = 2000;
        dpt = "HR";
    }

    // Member methods coming from the referenced type can be defined within the object.
    function getFullName() returns string {
        return firstName + " " + lastName; 
    }
};

// Member methods coming from the referenced type can also be defined outside of the object.
function Manager::getSalary() returns float {
        return self.salary;
}

public function main() {
    Manager p = new Manager(5, "John", "Doe");

    // Accessing fields that are coming from the referenced type.
    io:println(p.age);
    io:println(p.dpt);

    // Invoking methods that are coming from the referenced type.
    io:println(p.getFullName());
    io:println(p.getSalary());
}
