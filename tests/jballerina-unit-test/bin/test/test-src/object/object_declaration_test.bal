Person p = new;

function testGetDefaultValuesInObjectGlobalVar() returns [int, string, int, string] {
    return [p.age, p.emp.name, p.foo.key, p.bar.address];
}

function testGetDefaultValuesInObject() returns [int, string, int, string] {
    Person p1 = new;
    return [p1.age, p1.emp.name, p1.foo.key, p1.bar.address];
}

class Person {
    public int age = 0;
    public string name = "";
    public Employee emp = new;
    public Foo foo = new;
    public Bar bar = new;
}

class Employee {
    public int age = 0;
    public string name = "";

    isolated function init (int age = 6, string key = "abc") {
        self.age = age;
        self.name = "sample value";
    }
}

class Foo {
    public int key = 0;
    public string value = "";

    isolated function init () {
    }
}

class Bar {
    public string address = "";
}
