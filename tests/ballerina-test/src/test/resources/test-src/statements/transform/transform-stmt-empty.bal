struct Person {
    string firstName;
    string lastName;
    int age;
    string city;
}

struct Employee {
    string name;
    int age;
    string address;
}

function emptyTransform() returns (Employee) {
    Person p = {firstName:"John", lastName:"Doe", age:30, city:"London"};
    Employee e = <Employee, foo()> p;
    return e;
}

transformer <Person p, Employee e> foo() {
}
