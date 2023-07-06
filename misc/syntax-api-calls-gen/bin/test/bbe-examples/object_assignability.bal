import ballerina/io;

// Defines an object called `Person` with public fields and a method.
public class Person {
    public int age = 0;
    public string name = "";

    public function getName() returns string {
        return self.name;
    }
}

// Defines an object called `Employee` with public fields, methods, and the initializer.
public class Employee {
    public int age;
    public string name;
    public string address;

    public function init(int age, string name, string address) {
        self.age = age;
        self.name = name;
        self.address = address;
    }

    public function getName() returns string {
        return self.name + " Doe";
    }

    public function getAge() returns int {
        return self.age;
    }
}

public function main() {
    // Creates an object of the type `Employee` and assigns that to a variable of the type `Person`.
    Person p1 = new Employee(50, "John", "street1");

    io:println(p1.getName());
}
