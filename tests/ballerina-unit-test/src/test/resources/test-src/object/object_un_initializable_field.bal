
function test () returns int {
    Person p = new(8, new (9));
    return p.emp.age;
}

type Person object {

    public int age = 0;
    public Employee emp;


    new (age, emp) {

    }
};

type Employee object {

    public int age = 0;
    public Foo foo = new(0);
    public Bar bar = {};

    new (age) {

    }
};

type Foo object {

    public int calc = 0;

    new (calc) {

    }
};

type Bar record {
    int barVal = 0;
    string name = "";
};




