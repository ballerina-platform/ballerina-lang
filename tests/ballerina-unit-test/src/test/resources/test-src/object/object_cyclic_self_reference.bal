
function test () returns int {
    Person p = new();
    return p.emp.age;
}

type Person object {

    public int age;
    public Employee emp;

};

type Employee object {

    public int age;
    public Foo foo;
    public Bar bar;

};

type Foo object {

    public int calc;
    public Bar bar1;

};

type Bar record {
    int barVal;
    string name;
    Person person;
};




