
type Person object {

    public int age = 0;


    new (age) {

    }
};

type Employee object {

    public int age = 0;


    new (age, int addVal) {
        self.age = self.age + addVal;
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


