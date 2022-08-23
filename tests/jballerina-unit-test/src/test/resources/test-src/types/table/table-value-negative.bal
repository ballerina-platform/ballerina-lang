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

type Foo record {
    readonly map<string> m;
    int age;
};

type GlobalTable2 table<Foo> key(m);

function testTableConstructExprWithDuplicateKeys() returns string {
    GlobalTable2 tab2 = table [
      { m: {"AAA":"DDDD"}, age: 31 },
      { m: {"AAA":"DDDD"}, age: 34 }
    ];

    return tab2.toString();
}

const int idNum = 1;
function testVariableNameFieldAsKeyField() {
    table<record {readonly int idNum; string name;}> key (idNum) _ = table [
        {idNum, name: "Jo"},
        {idNum, name: "Chiran"},
        {idNum: 2, name: "Amy"}
    ];
}

function testVariableNameFieldAsKeyField2() {
    table<record {readonly int idNum = 1; string name;}> key (idNum) _ = table [
        { name: "Jo" },
        { name: "Chiran" },
        { idNum: 2, name: "Amy" }
    ];
}

type Foo2 record {
    readonly int a = 1;
    int b;
};

type GlobalTable3 table<Foo2> key(a);

function testTableConstructExprWithDuplicateDefaultKeys() {
    GlobalTable3 _ = table [
        { a: 2, b: 5 },
        { a: 2, b: 5 },
        { b: 3 },
        { b: 5 }
    ];
}

type Foo3 record {
    readonly int a = 1;
    readonly string b = "A";
};

type GlobalTable4 table<Foo3> key(a,b);

function testTableConstructExprWithDuplicateDefaultKeys2() {
    GlobalTable4 _ = table [
        { a: 2, b: "" },
        { a: 2, b: "" },
        { b: "" },
        { b: "" }
    ];
}

function testTableConstructExprWithDuplicateDefaultKeys3() {
    GlobalTable4 _ = table [
        {},
        {}
    ];
}

type Foo4 record {
    readonly map<string> m = {"AAA":"DDDD"};
    readonly int key = 23;
};

type GlobalTable5 table<Foo4> key(m, key);

function testTableConstructExprWithDuplicateDefaultKeys4() {
    GlobalTable5 _ = table [
      { key: 31 },
      { key: 31 },
      {},
      {}
    ];
}
