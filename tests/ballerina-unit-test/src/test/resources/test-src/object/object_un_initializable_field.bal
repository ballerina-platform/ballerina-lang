
function test () returns int {
    Person p = new(8, new (9));
    return p.emp.age;
}

type Person object {

    public int age;
    public Employee emp;

    __init (age, emp) {
    }
};

type Employee object {
    public int age = 0;
    public Foo foo;
    public Bar bar = {};

    __init (age) {
    }
};

type Foo object {

    public int calc;

    __init (calc) {
    }
};

type Bar record {
    int barVal = 0;
    string name = "";
};
