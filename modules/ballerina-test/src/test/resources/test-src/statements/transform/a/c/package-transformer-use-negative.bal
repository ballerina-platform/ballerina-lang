package a.c;

import p.q;

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

function testTransformer() (string, int, string) {
    Person p = {fname:"Jane", lname:"Doe", age:25, city:"Paris"};
    Employee e = <Employee, q:Foo()> p;
    return e.name, e.age, e.address;
}
