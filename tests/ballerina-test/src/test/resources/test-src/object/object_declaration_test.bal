Person p;

function testGetDefaultValuesInObjectGlobalVar() returns (int, string, int, string) {
    return (p.age, p.emp.name, p.foo.key, p.bar.address);
}

function testGetDefaultValuesInObject() returns (int, string, int, string) {
    Person p;
    return (p.age, p.emp.name, p.foo.key, p.bar.address);
}

type Person object {
    public {
        int age,
        string name,
        Employee emp,
        Foo foo,
        Bar bar,
    }
};

type Employee object {
    public {
        int age,
        string name,
    }

    new (age = 6, string key = "abc") {
        name = "sample value";
    }
};

type Foo object {
    public {
        int key,
        string value,
    }
    new () {

    }
};

type Bar object {
    public {
        string address,
    }

};