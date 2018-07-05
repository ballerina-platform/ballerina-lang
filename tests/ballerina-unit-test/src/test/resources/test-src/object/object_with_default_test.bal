
function testGetDefaultValuesInObjectFields() returns (int, string) {
    Person p = new (5, "world");
    return (p.age, p.name);
}

type Person object {
    public {
        int age = 10,
        string name = "hello ",
    }
    new (int age, string name) {
        self.age = self.age + age;
        self.name = self.name + name;
    }
};
