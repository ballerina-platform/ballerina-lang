type Person1 object {
    string name;
    int age;

    public function __init(string name, int age) {
        self.modify(self);
        self.name = name;
        self.age = age;
    }

    function modify(Person1 person) {
        person.name = "New Name";
    }
};

public function testSelfKeywordInvocationNegative() {
    Person1 person = new("person1", 32);
}

type Person2 object {
    string name;
    string city;

    public function __init(string name, string city) {
        int i = 0;
        while (i < 5) {
            self.modify(self);
        }
        self.name = name;
        self.city = city;
    }

    function modify(Person2 person) {
        person.name = "New Name";
    }
};

public function testSelfKeywordInvocationWithLoopNegative() {
    Person2 person = new("person2", "city");
}

type Person3 object {
    string name;
    string city;

    public function __init(string name) {
        if (name == "") {
            self.modify(self);
        }
        self.name = name;
        self.city = "city";
    }

    function modify(Person3 person) {
        person.name = "New Name";
    }
};

public function testSelfKeywordInvocationWithBranchNegative() {
    Person3 person = new("person3");
}