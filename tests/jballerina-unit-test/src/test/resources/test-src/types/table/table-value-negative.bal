type Person record {
    readonly string name;
    int age;
};

type Customer record {
    readonly int id;
    readonly string name;
    string lname;
};

type CustomerTable table<Customer> key(id, name);

type PersonTable table<Person> key(name);

PersonTable tab1 = table [
    { name: "AAA", age: 31 },
    { name: "AAA", age: 34 }
    ];

CustomerTable tab2 = table [
    { id: 13 , name: "Foo", lname: "QWER" },
    { id: 13 , name: "Foo" , lname: "UYOR" }
    ];

