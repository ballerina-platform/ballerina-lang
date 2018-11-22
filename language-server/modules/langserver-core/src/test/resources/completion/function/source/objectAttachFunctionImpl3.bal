import ballerina/io;

type Person object {
    public int age;
    public string firstName;
    public string lastName;
    new(age, firstName, lastName) {
    }
    function getFullName() returns string;
    function checkAndModifyAge(int condition, int a);
};

function Person.

function main(string... args) {
    Person p1 = new(5, "John", "Doe");
    io:println(p1);
    io:println(p1.getFullName());
    p1.checkAndModifyAge(10, 50);
    io:println(p1);
}
