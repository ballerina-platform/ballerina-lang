
type Person object {
    public {
        int age,
    }

    new (age) {

    }
};

type Employee object {
    public {
        int age,
    }

    new (age, int addVal) {
        age = age + addVal;
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
    error person2 = new (5);
}


