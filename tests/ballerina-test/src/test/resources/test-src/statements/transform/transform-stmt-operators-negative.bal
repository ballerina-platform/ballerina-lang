struct Person {
    string firstName;
    string lastName;
    int age;
    string city;
}

struct Employee {
    string name;
    int age;
    boolean retired;
    string address;
}

function operatorsInTransform() returns (string, int, boolean){
    Person p = {firstName:"John", lastName:"Doe", age:30, city:"London"};
    string prefix = "Ms.";
    Employee e = <Employee, Foo(prefix)> p;
    return (e.name, e.age, e.retired);
}

transformer <Person p, Employee e> Foo(string prefix) {
    e.name = prefix + p.firstName;
    p.firstName = "Ann";
    e.retired = (p.age > 60) ? true: false;
    p.age = 45;
}