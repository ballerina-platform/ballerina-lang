type Person record {
    readonly string name;
    readonly int age;
};

type Customer record {
    readonly int id;
    readonly string name;
    string lname;
};

type CustomerTable table<Customer> key<anydata>;

type PersonTable table<Person|Customer> key<int>;

PersonTable tab1 = table key(age) [
    { name: "AAA", age: 31 },
    { name: "BBB", age: 34 }
    ];

CustomerTable tab2 = table key(name) [
    { id: 13 , name: "Foo", lname: "QWER" },
    { id: 13 , name: "Bar" , lname: "UYOR" }
    ];

function testKeyConstraintCastToString() returns boolean {

    table<Person> key<int> tab =<table<Person> key<int>> tab1;
    return tab[31]["name"] == "AAA";
}
