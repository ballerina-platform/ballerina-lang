Person p = new;

function testGetDefaultValuesInObjectGlobalVar() returns [int, string, int, string] {
    return [p.age, p.emp.name, p.foo.key, p.bar.address];
}

function testGetDefaultValuesInObject() returns [int, string, int, string] {
    Person p1 = new;
    return [p1.age, p1.emp.name, p1.foo.key, p1.bar.address];
}

type Person object {
    public int age = 0;
    public string name = "";
    public Employee emp = new;
    public Foo foo = new;
    public Bar bar = new;
};

type Employee object {
    public int age = 0;
    public string name = "";

    function __init (int age = 6, string key = "abc") {
        self.age = age;
        self.name = "sample value";
    }
};

type Foo object {
    public int key = 0;
    public string value = "";

    function __init () {
    }
};

type Bar object {
    public string address = "";
};
