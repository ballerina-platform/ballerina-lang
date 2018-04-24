
function testRecursiveObjectWithNill() returns int {
    Person p;
    return (p.age);
}


type Person object {
    public {
        int age = 90,
        Employee ep,
    }
};


type Employee object {
    public {
        int pp,
        Person? p,
    }
};
