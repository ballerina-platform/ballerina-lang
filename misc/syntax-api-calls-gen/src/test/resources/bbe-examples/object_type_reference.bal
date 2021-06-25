import ballerina/io;

// Defines an object type called `Person`. It should only contain fields and the
// method declarations.
type Person object {
    public int age;
    public string firstName;
    public string lastName;

    // Method declarations can be within the object. However, the method cannot
    // have a body.
    function getFullName() returns string;

};

// Defines another object type called `Employee`, which references the `Person` object.
type Employee object {
    // Add a reference to the `Person` object type. 
    // All the member fields and member-method declarations will be copied from the `Person` object.
    *Person;
    public float|string salary;

    function getSalary() returns float|string;
};

class Owner {
    public string status = "";
}

class Manager {
    // Type references can be chained by adding a reference to the `Employee` object, which
    // again has a reference to the `Employee` object. This will copy all the members from
    // the `Employee` object. It will be the same as declaring each of those members within this object.
    *Employee;

    // It is possible to have more than one type reference as well.
    *Owner;

    public string dpt;

    // Referenced fields can be overridden in a type-descriptor if the type of the field  
    // in the overriding descriptor is a sub-type of the original type of the field.
    public float salary;

    // All the fields referenced through the type reference can be accessed within this object.
    function init(int age, string firstName, string lastName, string status) {
        self.age = age;
        self.firstName = firstName;
        self.lastName = lastName;
        self.status = status;
        self.salary = 2000.0;
        self.dpt = "HR";
    }

    // The member methods coming from the referenced type should be defined within the object.
    function getFullName() returns string {
        return self.firstName + " " + self.lastName;
    }

    // Referenced methods can also be overridden as long as the method in the overriding 
    // descriptor is a sub-type of the method in the referenced type.
    function getSalary() returns float {
        return self.salary;
    }
}

public function main() {
    Manager p = new Manager(5, "John", "Doe", "Senior");

    // Accessing the fields that are coming from the referenced type.
    io:println(p.age);
    io:println(p.dpt);

    // Invoking the methods that are coming from the referenced type.
    io:println(p.getFullName());
    io:println(p.getSalary());
}
