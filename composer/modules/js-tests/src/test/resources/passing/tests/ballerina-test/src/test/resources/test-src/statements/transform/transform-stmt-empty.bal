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

function emptyTransform() (Employee) {
    Person p = {firstName:"John", lastName:"Doe", age:30, city:"London"};
    Employee e = <Employee> p;
    return e;
}

transformer <Person p, Employee e> {
}
