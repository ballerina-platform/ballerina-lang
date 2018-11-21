
import initializers as inp;

public type person object {

    public int age = 0;
    public string name = "";
    public string address = "";


    new (int a = 10, string n = "Charles") {
        self.name = n;
        self.age = a;
    }

    function getAge();
};

function person.getAge() {
    self.age = 12;
}

function testObjectInitializerInSamePackage1() returns (int, string){
    person p = new(n = "Peter");
    return (p.age, p.name);
}

function testObjectInitializerInAnotherPackage() returns (int, string){
    inp:employee e = new("Peter");
    return (e.age, e.name);
}

type employee object {

    public int age = 0;
    public string name = "A";


    //TODO: define construvtor as new (int age = 30, string name) once
    // https://github.com/ballerina-platform/ballerina-lang/issues/6849 is fixed.
    new (int a = 30, name) {
        self.name = self.name + name;
        self.age = a;
    }
};

function testObjectInitializerOrder() returns (int, string){
    employee p = new (a = 40, "B");
    return (p.age, p.name);
}
