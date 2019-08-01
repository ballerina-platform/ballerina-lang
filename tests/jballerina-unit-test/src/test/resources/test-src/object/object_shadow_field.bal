public function testShadowingObjectField () returns [int, string] {
    Person p = new Person(a = 50, n = "passed in name value");
    return [p.age, p.name];
}

type Person object {
    public int age = 10;
    public string name = "";

    function __init (int a = 10, string n = "sample result") {
        self.age = a;
        self.name = n;
    }
};
