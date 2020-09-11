class Person {
    public int age = 0;

    function init (int age) {
        self.age = age;
    }
}

class Employee {
    public int age = 0;

    function init (int age, int addVal) {
        self.age = age + addVal;
    }
}

function returnDifferentObectInit() returns Person {
    return new (5, 7);
}

function returnDifferentObectInit1() returns Person | () {
    return new (5);
}

function returnDifferentObectInit2() {
    Person | () person = new (5);
    var person1 = new (5);
}

class Student {
    string name = "";
}

function returnDifferentObectInit3() {
    Student? student = new("student-1");
}

function returnDifferentObectInit4() {
    Student student = new("student-2");
}
