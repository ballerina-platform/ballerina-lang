
function test () returns int {
    Person p = new(8, new (9));
    return p.emp.age;
}

type Person object {
    public {
        int age,
        Employee emp,
    }

    new (age, emp) {

    }
};

type Employee object {
    public {
        int age,
        Foo foo,
        Bar bar,
    }
    new (age) {

    }
};

type Foo object {
    public {
        int calc,
    }

    new (calc) {

    }
};

type Bar {
    int barVal,
    string name,
};




