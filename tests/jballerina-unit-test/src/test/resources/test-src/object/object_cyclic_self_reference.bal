
function test () returns int {
    Person p = new();
    return p.emp.age;
}

class Person {
    public int age = 0;
    public Employee emp = new;
}

class Employee {
    public int age = 0;
    public Foo foo = new;
    public Bar bar = {};
}

class Foo {
    public int calc = 0;
    public Bar bar1 = {};
}

type Bar record {
    int barVal = 0;
    string name = "";
    Person person = new;
};
