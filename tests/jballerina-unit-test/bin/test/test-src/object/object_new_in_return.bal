function testCreateObjectInReturnSameType () returns int {
    return returnSameObectInit().age;
}

function testCreateObjectInReturnDifferentType () returns int {
    return returnDifferentObectInit().age;
}

class Person {
    public int age = 0;

    function init (int age) {
        self.age = age;
    }
}

class Employee {
    public int age = 0;

    function init (int age, int addVal) {
        self.age = age + addVal;
    }
}

function returnSameObectInit() returns Person {
    return new (5);
}

function returnDifferentObectInit() returns Person {
    return new Employee(5, 7);
}
