type Person record {
    readonly string name;
    int age;
};

//type Foo record {
//    readonly map<string> m;
//    int age;
//};

type GlobalTable1 table<Person> key(name);
//type GlobalTable2 table<Foo> key(m);

GlobalTable1 tab1 = table [
  { name: "AAA", age: 31 },
  { name: "BBB", age: 34 }
];

function testGlobalTableConstructExpr() returns boolean {
    return tab1.toString() == "name=AAA age=31\nname=BBB age=34";
}

//function testTableConstructExprWithDuplicateKeys() returns string {
//    GlobalTable2 tab2 = table [
//      { m: {"AAA":"DDDD"}, age: 31 },
//      { m: {"AAA":"DDDD"}, age: 34 }
//    ];
//
//    return tab2.toString();
//}

function testTableMemberAccessStore() returns boolean {
    GlobalTable2 tab = table [
      { m: {"AAA":"DDDD"}, age: 31 },
      { m: {"BBB":"DDDD"}, age: 34 }
    ];

    tab[{"CCC":"EEEE"}] = { m: {"CCC":"EEEE"}, age: 34 };
    return tab.toString() == "m=AAA=DDDD age=31\nm=BBB=DDDD age=34\nm=CCC=EEEE age=34";
}

function testTableMemberAccessLoad() returns boolean {
    GlobalTable2 tab = table [
      { m: {"AAA":"DDDD"}, age: 31 },
      { m: {"BBB":"DDDD"}, age: 34 }
    ];
    Foo aaa = tab[{"AAA":"DDDD"}];
    return aaa.toString() == "m=AAA=DDDD age=31";
}

type Customer record {
    readonly int id;
    readonly string name;
    string address;
};

string cutomerListString = "id=13 name=Sanjiva address=Weerawarana\nid=23 name=James address=Clark";

type CustomerTableWithKS table<Customer> key(id);

function runKeySpecifierTestCases() {
    testTableTypeWithKeySpecifier();
    testTableConstructorWithKeySpecifier();
    testTableTypeWithCompositeKeySpecifier();
    testTableConstructorWithCompositeKeySpecifier();
    testTableTypeWithKeyTypeConstraint();
    testTableTypeWithCompositeKeyTypeConstraint();
}

function testTableTypeWithKeySpecifier() {
    CustomerTableWithKS tab = table [{ id: 13 , name: "Sanjiva", address: "Weerawarana" },
                                    { id: 23 , name: "James" , address: "Clark" }];

    assertEquality(cutomerListString, tab.toString());
}

function testTableConstructorWithKeySpecifier() {
    CustomerTableWithKS tab = table key(id) [{ id: 13 , name: "Sanjiva", address: "Weerawarana" },
                                    { id: 23 , name: "James" , address: "Clark" }];

    assertEquality(cutomerListString, tab.toString());
}

type CustomerTableWithCKS table<Customer> key(id, name);

function testTableTypeWithCompositeKeySpecifier() {
    CustomerTableWithCKS tab = table [{ id: 13 , name: "Sanjiva", address: "Weerawarana" },
                                    { id: 23 , name: "James" , address: "Clark" }];

    assertEquality(cutomerListString, tab.toString());
}

function testTableConstructorWithCompositeKeySpecifier() {
    CustomerTableWithCKS tab = table key(id, name) [{ id: 13 , name: "Sanjiva", address: "Weerawarana" },
                                    { id: 23 , name: "James" , address: "Clark" }];

    assertEquality(cutomerListString, tab.toString());
}

type CustomerTableWithKTC table<Customer> key<int>;

function testTableTypeWithKeyTypeConstraint() {
    CustomerTableWithKTC tab = table key(id) [{ id: 13 , name: "Sanjiva", address: "Weerawarana" },
                                    { id: 23 , name: "James" , address: "Clark" }];

    assertEquality(cutomerListString, tab.toString());
}

type CustomerTableWithCKTC table<Customer> key<[int, string]>;

function testTableTypeWithCompositeKeyTypeConstraint() {
    CustomerTableWithCKTC tab = table key(id, name) [{ id: 13 , name: "Sanjiva", address: "Weerawarana" },
                                    { id: 23 , name: "James" , address: "Clark" }];

    assertEquality(cutomerListString, tab.toString());
}

type AssertionError error<ASSERTION_ERROR_REASON>;

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

function assertFalse(any|error actual) {
    assertEquality(false, actual);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic AssertionError(message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
