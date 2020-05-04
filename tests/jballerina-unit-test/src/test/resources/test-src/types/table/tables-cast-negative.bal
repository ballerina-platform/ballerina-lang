type Person record {
    readonly string name;
    int age;
};

type Customer record {
    readonly int id;
    readonly string name;
    string lname;
};

type CustomerTable table<Customer> <anydata>;

type PersonTable table<Person> key<anydata>;

PersonTable tab1 = table key(name) [
    { name: "AAA", age: 31 },
    { name: "BBB", age: 34 }
    ];

CustomerTable tab2 = table key(name) [
    { id: 13 , name: "Foo", lname: "QWER" },
    { id: 13 , name: "Foo" , lname: "UYOR" }
    ];

function testkeyConstraintCastToString() returns boolean {
    table<Person> key<string> tab = tab1;
    return tab["AAA"]["name"] == "AAA";
}
