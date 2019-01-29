import initializers as inp;

public type person object {

    public int age = 0;
    public string name = "";
    public string address = "";

    function __init (int a = 10, string n = "Charles") {
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

    function __init (int a = 30, string name) {
        self.name = self.name + name;
        self.age = a;
    }
};

function testObjectInitializerOrder() returns (int, string){
    employee p = new (a = 40, "B");
    return (p.age, p.name);
}

function testObjectInitializerUsedAsAFunction() returns (int, string, int, string) {
    person p = new(n = "Peter");
    int age1 = p.age;
    string name1 = p.name;
    p.age = 15;
    p.name = "Jack";

    p.__init(a = 20, n = "James");
    return (p.age, p.name, age1, name1);
}
