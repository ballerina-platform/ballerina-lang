public function testShadowingObjectField () returns [int, string] {
    Person p = new Person(a = 50, n = "passed in name value");
    return [p.age, p.name];
}

class Person {
    public int age = 10;
    public string name = "";

    function init (int a = 10, string n = "sample result") {
        self.age = a;
        self.name = n;
    }
}
