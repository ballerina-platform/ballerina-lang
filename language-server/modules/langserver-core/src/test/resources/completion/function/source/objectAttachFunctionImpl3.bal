import ballerina/io;

type Person object {
    public int age = -1;
    public string firstName = "";
    public string lastName = "";
    public function __init(int age, string firstName, string lastName) {
        self.age = age;
        self.firstName = firstName;
        self.lastName = lastName;
    }
    function getFullName() returns string;
    function checkAndModifyAge(int condition, int a);
};

function Person.

public function main(string... args) {
    Person p1 = new(5, "John", "Doe");
    io:println(p1);
    io:println(p1);
}
