Person? p1 = new;
Person? p2 = new ();

class Person {
    public int age = 0;
}

class Employee {

    public Person? p3 = new;
    public Person? p4 = new ();
    public Person? p5 = ();
    public Person? p6 = ();

    function init () {
        self.p5 = new;
        self.p6 = new();
    }
}

function getEmployeeInstance() returns Employee {
    return new Employee();
}

function getPersonInstances() returns [Person?, Person?, Person?, Person?, Person?, Person?] {
    Person? p3 = new;
    Person? p4 = new();
    Person? p5 = ();
    p5 = new;
    Person? p6 = ();
    p6 = new ();
    return [p1, p2, p3, p4, p5, p6];
}
