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
    any ageAny;
}

documentation {
 Transformer Foo Person -> Employee
 - #p input struct Person source used for transformation
 - #e output struct Employee struct which Person transformed to
 - #defaultAddress address which serves Eg: `POSTCODE 112`
}
transformer <Person p, Employee e> Foo(any defaultAddress) {
    e.name = p.firstName;
}
