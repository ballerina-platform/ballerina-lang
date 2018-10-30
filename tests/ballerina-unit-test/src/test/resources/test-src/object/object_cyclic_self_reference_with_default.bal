
function testCyclicReferenceWithDefaultable () returns int {
    Person p = new();
    Employee em = new;
    em.age = 89;
    p.emp = em;
    return (p.emp.age ?: 56);
}

type Person object {
    public int age;
    public Employee? emp;
};

type Employee object {
    public int age;
    public Foo? foo;
    public Bar? bar;
};

type Foo object {
    public int calc;
    public Bar? bar1;
};

type Bar record {
    int barVal;
    string name;
    Person? person;
};




