import projectls.ls.mod4;

function main() {
    mod4:TestClassEntity e = new();
    e.
}

public service class Mod1Class {

    private string name;
    private int age;
    public final string PERSON_KW = "PERSON";

    function init(string name, int age) {
        self.name = name;
        self.age = age;
    }

    public function getName() returns string {
        return self.name;
    }

    resource function getName .() returns string {
        return self.name;
    }

    remote function getAge() returns int {
        return self.age;
    }

    private function incrementAge() {
        self.age += 1;
    }
}
