type Person record {
    string name;
    int age;
};

type Foo record {
    map<string> m;
    int age;
};

type GlobalTable1 table<Person> key(name);
type GlobalTable2 table<Foo> key(m);


GlobalTable1 tab1 = table [
  { name: "AAA", age: 31 },
  { name: "BBB", age: 34 }
];

GlobalTable2 tab2 = table [
  { m: {"AAA":"DDDD"}, age: 31 },
  { m: {"AAA":"DDDD"}, age: 34 }
];

function testGlobalTableConstructExpr1() returns string {
    return tab1.toString();
}

function testGlobalTableConstructExpr2() returns string {
    return tab2.toString();
}

type Customer record {
    int id;
    string name;
    string address;
};

Customer[] cutomerList = [{ id: 13 , name: "Sanjiva", address: "Weerawarana" },
                                { id: 23 , name: "James" , address: "Clark" }];

type CustomerTableWithKS table<Customer> key(id);

function runKeySpecifierTestcases() {
    testTableTypeWithKeySpecifier();
    testTableConstructorWithKeySpecifier();
    testTableTypeWithCompositeKeySpecifier();
    testTableConstructorWithCompositeKeySpecifier();
    //testTableTypeWithKeyTypeConstraint();
    //testTableTypeWithCompositeKeyTypeConstraint();
}

function testTableTypeWithKeySpecifier() {
    CustomerTableWithKS tab = table [{ id: 13 , name: "Sanjiva", address: "Weerawarana" },
                                    { id: 23 , name: "James" , address: "Clark" }];

    assertEquality(cutomerList.toString(), tab.toString());
}

function testTableConstructorWithKeySpecifier() {
    CustomerTableWithKS tab = table key(id) [{ id: 13 , name: "Sanjiva", address: "Weerawarana" },
                                    { id: 23 , name: "James" , address: "Clark" }];

    assertEquality(cutomerList.toString(), tab.toString());
}

type CustomerTableWithCKS table<Customer> key(id, name);

function testTableTypeWithCompositeKeySpecifier() {
    CustomerTableWithCKS tab = table [{ id: 13 , name: "Sanjiva", address: "Weerawarana" },
                                    { id: 23 , name: "James" , address: "Clark" }];

    assertEquality(cutomerList.toString(), tab.toString());
}

function testTableConstructorWithCompositeKeySpecifier() {
    CustomerTableWithCKS tab = table key(id, name) [{ id: 13 , name: "Sanjiva", address: "Weerawarana" },
                                    { id: 23 , name: "James" , address: "Clark" }];

    assertEquality(cutomerList.toString(), tab.toString());
}

type CustomerTableWithKTC table<Customer> key<int>;

function testTableTypeWithKeyTypeConstraint() {
    CustomerTableWithKTC tab = table key(id) [{ id: 13 , name: "Sanjiva", address: "Weerawarana" },
                                    { id: 23 , name: "James" , address: "Clark" }];

    assertEquality(cutomerList.toString(), tab.toString());
}

type CustomerTableWithCKTC table<Customer> key<[int, string]>;

function testTableTypeWithCompositeKeyTypeConstraint() {
    CustomerTableWithCKTC tab = table key(id, name) [{ id: 13 , name: "Sanjiva", address: "Weerawarana" },
                                    { id: 23 , name: "James" , address: "Clark" }];

    assertEquality(cutomerList.toString(), tab.toString());
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
