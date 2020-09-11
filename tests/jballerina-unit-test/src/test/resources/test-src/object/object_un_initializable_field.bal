function test () returns int {
    Person p = new(8, new (9));
    return p.emp.age;
}

class Person {
    public int age = 0;
    public Employee emp;

    function init (int age, Employee emp) {
        self.age = age;
        self.emp = emp;
    }
}

class Employee {
    public int age = 0;
    public Foo foo;
    public Bar bar = {};

    function init (int age) {
        self.age = age;
    }
}

class Foo {
    public int calc;

    function init (int calc) {
        self.calc = calc;
    }
}

type Bar record {
    int barVal = 0;
    string name = "";
};
