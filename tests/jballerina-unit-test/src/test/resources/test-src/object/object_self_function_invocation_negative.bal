class Person1 {
    string name;
    int age;

    public function init(string name, int age) {
        self.modify(self);
        self.name = name;
        self.age = age;
    }

    function modify(Person1 person) {
        person.name = "New Name";
    }
}

public function testSelfKeywordInvocationNegative() {
    Person1 person = new("person1", 32);
}

class Person2 {
    string name;
    string city;

    public function init(string name, string city) {
        int i = 0;
        while (i < 5) {
            if (i/2 == 2) {
                continue;
            }
            self.modify(self);
        }
        self.name = name;
        self.city = city;
    }

    function modify(Person2 person) {
        person.name = "New Name";
    }
}

public function testSelfKeywordInvocationWithLoopAndContinueNegative() {
    Person2 person = new("person2", "city");
}

class Person3 {
    string name;
    string city;

    public function init(string name, string city) {
        int i = 0;
        while (i < 5) {
            if (i/2 == 2) {
                break;
            }
            self.modify(self);
        }
        self.name = name;
        self.city = city;
    }

    function modify(Person3 person) {
        person.name = "New Name";
    }
}

public function testSelfKeywordInvocationWithLoopAndBreakNegative() {
    Person3 person = new("person2", "city");
}


class Person4 {
    string name;
    string city;

    public function init(string name) {
        if (name == "") {
            self.name = "tom";
          } else if (name == "tom") {
            self.city = "London";
          } else {
            self.name = "tom";
            self.city = "London";
          }
        self.modify(self);
        self.name = name;
        self.city = "city";
    }

    function modify(Person4 person) {
        person.name = "New Name";
    }
}

public function testSelfKeywordInvocationWithBranchNegative() {
    Person4 person = new("");
}

class Person5 {
    string name;
    string city;

    public function init(string[] names, string city) {
        foreach string n in names {
            if (n == "person1") {
                self.modify(self);
            }
            self.name = n;
        }
        self.city = city;
    }

    function modify(Person5 person) {
        person.name = "New Name";
    }
}

public function testSelfKeywordInvocationWithForEachNegative() {
    string[] persons = ["person1", "person2", "person3"];
    Person5 person = new(persons, "city");
}

class Person6 {
    string name;
    string city;

    public function init(string[] names, string city) {
        foreach string n in names {
            if (n == "person1") {
                self.modify("person1");
            }
            self.name = n;
        }
        self.city = city;
    }

    function modify(string name) {
        self.name = name;
    }
}

public function testSelfKeywordInvocationWithInvocationArg() {
    string[] persons = ["person1", "person2", "person3"];
    Person5 person = new(persons, "city");
}


class Person7 {
    string name;

    public function init(string name) {
        modify(self);
        self.name = name;
    }
}

function modify(Person7 person) {

}

public function testSelfKeywordInvocationWithModuleLevelFunctionInvocation() {
    Person7 person = new("person");
}

class Person8 {
    string name;

    public function init(string name) {
        change(self);
        self.name = name;
    }
}

function change(Person8... person) {
}

public function testSelfKeywordInvocationWithModuleLevelFunctionInvocationWithRestArgs() {
    Person8 person = new("person");
}
