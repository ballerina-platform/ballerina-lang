Person p;

function testGetDefaultValuesInObjectGlobalVar() returns (int, string, int, string) {
    return (p.age, p.emp.name, p.foo.key, p.bar.address);
}

function testGetDefaultValuesInObject() returns (int, string, int, string) {
    Person p1;
    return (p1.age, p1.emp.name, p1.foo.key, p1.bar.address);
}

type Person object {
    public int age,
    public string name,
    public Employee emp,
    public Foo foo,
    public Bar bar,
};

type Employee object {
    public int age,
    public string name,

    new (age = 6, string key = "abc") {
        name = "sample value";
    }
};

type Foo object {
    public int key,
    public string value,

    new () {

    }
};

type Bar object {
    public string address,
};
