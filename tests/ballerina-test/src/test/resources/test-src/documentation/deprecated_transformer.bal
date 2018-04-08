type Person {
    string firstName;
    string lastName;
    int age;
    string city;
};

type Employee {
    string name;
    int age;
    string address;
    any ageAny;
};

documentation {
 Transformer Foo Person -> Employee
 T{{p}} input struct Person source used for transformation
 T{{e}} output struct Employee struct which Person transformed to
 P{{defaultAddress}} address which serves Eg: `POSTCODE 112`
}
deprecated {
  This Transformer is deprecated use
  `transformer <Person p, Employee e> Bar(any defaultAddress) { e.name = p.firstName; }
  ` instead.
}
transformer <Person p, Employee e> Foo(any defaultAddress) {
    e.name = p.firstName;
}
