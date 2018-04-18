
function testCyclicReferenceWithDefaultable () returns int {
    Person p = new();
    Employee em = new;
    em.age = 89;
    p.emp = em;
    return (p.emp.age ?: 56);
}

type Person object {
    public {
        int age,
        Employee? emp,
    }
};

type Employee object {
    public {
        int age,
        Foo? foo,
        Bar? bar,
    }
};

type Foo object {
    public {
        int calc,
        Bar? bar1,
    }
};

type Bar {
    int barVal,
    string name,
    Person? person,
};




