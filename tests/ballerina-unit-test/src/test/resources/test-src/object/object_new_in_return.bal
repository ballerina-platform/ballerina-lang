function testCreateObjectInReturnSameType () returns int {
    return returnSameObectInit().age;
}

function testCreateObjectInReturnDifferentType () returns int {
    return returnDifferentObectInit().age;
}

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

function returnSameObectInit() returns Person {
    return new (5);
}

function returnDifferentObectInit() returns Person {
    return new Employee(5, 7);
}
