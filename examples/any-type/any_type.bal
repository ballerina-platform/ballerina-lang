import ballerina/io;

type Person object {
    string name;
    int age;

    function __init(string name = "John", int age = 20) {
        self.name = name;
        self.age = age;
    }

    public function getAge() returns int {
        return self.age;
    }
};

public function main() {
    int a = 25;
    io:println(a);

    any p = new Person(age=a);
    Person|error personVal = trap <Person>p;
    if (personVal is Person) {
        io:println("Person p's age: " + personVal.getAge());
    } else {
        // Runtime value is cast to correct type since Ballerina runtime can infer the correct type to error.
        io:println("Error occurred: " + personVal.reason());
    }
}
