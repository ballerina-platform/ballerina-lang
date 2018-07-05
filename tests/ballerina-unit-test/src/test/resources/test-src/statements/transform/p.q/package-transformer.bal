
public struct Person {
    string fname;
    string lname;
    int age;
    string city;
}

public struct Employee {
    string name;
    int age;
    string address;
}

public transformer <Person p, Employee e> Foo() {
    e.address = p.city;
    e.name = p.fname;
    e.age = p.age;
}