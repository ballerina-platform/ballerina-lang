
function test () returns int {
    Person p = new();
    return p.emp.age;
}

type Person object {
    public int age = 0;
    public Employee emp = new;
};

type Employee object {
    public int age = 0;
    public Foo foo = new;
    public Bar bar = {};
};

type Foo object {
    public int calc = 0;
    public Bar bar1 = {};
};

type Bar record {
    int barVal = 0;
    string name = "";
    Person person = new;
};
