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
    table<record {readonly int idNum = 2; string name;}> key (idNum) _ = table [
        {idNum, name: "Jo"},
        {idNum, name: "Chiran"},
        {idNum: 2, name: "Amy"}
    ];
}

function testVariableNameFieldAsKeyField3() {
    table<record {readonly int idNum = 2; readonly string name;}> key (idNum, name) _ = table [
        {idNum, name: "Jo"},
        {idNum, name: "Jo"},
        {idNum: 2, name: "Amy"}
    ];
}

function testVariableNameFieldAsKeyField4() {
    table<record {readonly int idNum = 2; readonly string name = "A";}> key (idNum, name) _ = table [
        {idNum, name: "Jo"},
        {idNum, name: "Jo"},
        {idNum: 2, name: "Amy"}
    ];
}

type Foo2 record {
    readonly map<string> m = {};
    int age;
};

type GlobalTable3 table<Foo2> key(m);

function testTableConstructExprWithDuplicateKeys2() returns string {
    GlobalTable3 tab = table [
      { m: {"AAA":"DDDD"}, age: 31 },
      { m: {"AAA":"DDDD"}, age: 34 }
    ];

    return tab.toString();
}

type Foo3 record {
    readonly map<string> m = {};
    readonly int age = 18;
};

type GlobalTable4 table<Foo3> key(m, age);

function testTableConstructExprWithDuplicateKeys3() returns string {
    GlobalTable4 tab = table [
      { m: {"AAA":"DDDD"}, age: 11 },
      { m: {"AAA":"DDDD"}, age: 11 }
    ];

    return tab.toString();
}
