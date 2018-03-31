struct Person {
    string firstname;
    string lastname;
    int age;
    string city;
}

struct Employee {
    string name;
    int age;
    string address;
}

function oneToOneTransform() returns (string, int, string){
    Person p = {firstname:"John", lastname:"Doe", age:30, city:"London"};
    Employee e = <Employee, Foo()> p;
    return (e.name, e.age, e.address);
}

transformer <Person p, Employee e> Foo() {
    e.address = p.city;
    p.firstname = e.name;
    e.age = p.age;
}
