
function test () returns int {
    Person p = new();
    return p.emp.age;
}

type Person object {
    public {
        int age,
        Employee emp,
    }
};

type Employee object {
    public {
        int age,
        Foo foo,
        Bar bar,
    }
};

type Foo object {
    public {
        int calc,
        Bar bar1,
    }
};

type Bar {
    int barVal,
    string name,
    Person person,
};




