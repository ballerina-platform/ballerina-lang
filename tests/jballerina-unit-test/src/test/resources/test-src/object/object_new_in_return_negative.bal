type Person object {
    public int age = 0;

    function __init (int age) {
        self.age = age;
    }
};

type Employee object {
    public int age = 0;

    function __init (int age, int addVal) {
        self.age = age + addVal;
    }
};

function returnDifferentObectInit() returns Person {
    return new (5, 7);
}

function returnDifferentObectInit1() returns Person | () {
    return new (5);
}

function returnDifferentObectInit2() {
    Person | () person = new (5);
    var person1 = new (5);
}

type Student object {
    string name = "";
};

function returnDifferentObectInit3() {
    Student? student = new("student-1");
}

function returnDifferentObectInit4() {
    Student student = new("student-2");
}
