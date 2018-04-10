
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



