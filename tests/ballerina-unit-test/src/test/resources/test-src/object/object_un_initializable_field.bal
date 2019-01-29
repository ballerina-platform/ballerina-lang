function test () returns int {
    Person p = new(8, new (9));
    return p.emp.age;
}

type Person object {
    public int age = 0;
    public Employee emp;

    function __init (int age, Employee emp) {
        self.age = age;
        self.emp = emp;
    }
};

type Employee object {
    public int age = 0;
    public Foo foo;
    public Bar bar = {};

    function __init (int age) {
        self.age = age;
    }
};

type Foo object {
    public int calc;

    function __init (int calc) {
        self.calc = calc;
    }
};

type Bar record {
    int barVal = 0;
    string name = "";
};
