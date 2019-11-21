type Person object {
    string name;

    public function __init(string name) {
        self.modify(self);
        self.name = name;
    }

    function modify(Person person) {
        person.name = "New Name";
    }
};

public function testSelfKeywordInvocationNegative() {
    Person person = new("person1");
}

