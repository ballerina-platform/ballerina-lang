import ballerina/module1;

function testFunction() {
    Person self = new();
    int personAge = self.getAge();
}

class Person {
    int age = 12;
    function getAge() returns int {
        return self.age;
    }
}
