
import initializers as inp;

public type person object {
    public {
        int age;
        string name;
        string address;
    }

    new (int age = 10, string name = "Charles") {
        self.name = name;
        self.age = age;
    }

    function getAge();
};

function person::getAge() {
    self.age = 12;
}

function testObjectInitializerInSamePackage1() returns (int, string){
    person p = new(name = "Peter");
    return (p.age, p.name);
}

function testObjectInitializerInAnotherPackage() returns (int, string){
    inp:employee e = new("Peter");
    return (e.age, e.name);
}

type employee object {
    public {
        int age;
        string name = "A";
    }

    //TODO: define construvtor as new (int age = 30, string name) once
    // https://github.com/ballerina-platform/ballerina-lang/issues/6849 is fixed.
    new (int age = 30, name) {
        self.name = self.name + name;
        self.age = age;
    }
};

function testObjectInitializerOrder() returns (int, string){
    employee p = new (age = 40, "B");
    return (p.age, p.name);
}
