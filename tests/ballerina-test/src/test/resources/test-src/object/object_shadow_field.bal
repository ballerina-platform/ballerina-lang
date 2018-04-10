

public function testShadowingObjectField () returns (int, string) {
    Person p = new Person(age = 50, name = "passed in name value");
    return (p.age, p.name);
}

type Person object {
    public {
        int age = 10,
        string name,
    }

    new (int age = 10, string name = "sample result") {
        self.age = age;
        self.name = name;
    }
};

