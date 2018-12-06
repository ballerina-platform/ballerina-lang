
function testRecursiveObjectWithNill() returns int {
    Person p = new;
    return (p.age);
}

type Person object {
    public int age = 90;
    public Employee ep = new;
};

type Employee object {
    public int pp = 0;
    public Person? p = ();
};
