type Person1 object {
    string name;

    public function __init(string name) {
        self.modify(self);
        self.name = name;
    }

    function modify(Person1 person) {
        person.name = "New Name";
    }
};

public function testSelfKeywordInvocationNegative() {
    Person1 person = new("person1");
}

type Person2 object {
    string name;

    public function __init(string name) {
        int i = 0;
        while (i < 5) {
            self.modify(self);
        }
        self.name = name;
    }

    function modify(Person2 person) {
        person.name = "New Name";
    }
};

public function testSelfKeywordInvocationWithLoopNegative() {
    Person2 person = new("person2");
}

type Person3 object {
    string name;

    public function __init(string name) {
        if (name == "") {
            self.modify(self);
        }
        self.name = name;
    }

    function modify(Person3 person) {
        person.name = "New Name";
    }
};

public function testSelfKeywordInvocationWithBranchNegative() {
    Person3 person = new("person3");
}