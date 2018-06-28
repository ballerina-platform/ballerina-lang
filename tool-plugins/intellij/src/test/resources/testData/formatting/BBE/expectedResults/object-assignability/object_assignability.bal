import ballerina/io;

// Defines an object called 'Person' with public fields and attached function.
public type Person object {
    public {
        int age,
        string name,
    }

    public function getName() returns string {
        return name;
    }
};

// Defines an object called 'Employee' with public fields and attached functions.
public type Employee object {
    public {
        int age,
        string name,
        string address,
    }

    public new(age, name, address) {

    }

    public function getName() returns string {
        return name + " Doe";
    }

    public function getAge() returns int {
        return age;
    }
};

function main(string... args) {
    // Initializing variable of object type Employee and assign that to Person type variable.
    Person p1 = new Employee(50, "John", "street1");
    io:println(p1);

    io:println(p1.getName());
}
