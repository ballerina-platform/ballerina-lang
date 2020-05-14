type Person record {
    readonly string name;
    int age;
};

//TODO: uncomment all when readonly is supported for complex records also
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

//function testTableMemberAccessStore() returns boolean {
//    GlobalTable2 tab = table [
//      { m: {"AAA":"DDDD"}, age: 31 },
//      { m: {"BBB":"DDDD"}, age: 34 }
//    ];
//
//    tab[{"CCC":"EEEE"}] = { m: {"CCC":"EEEE"}, age: 34 };
//    return tab.toString() == "m=AAA=DDDD age=31\nm=BBB=DDDD age=34\nm=CCC=EEEE age=34";
//}

//function testTableMemberAccessLoad() returns boolean {
//    GlobalTable2 tab = table [
//      { m: {"AAA":"DDDD"}, age: 31 },
//      { m: {"BBB":"DDDD"}, age: 34 }
//    ];
//    Foo aaa = tab[{"AAA":"DDDD"}];
//    return aaa.toString() == "m=AAA=DDDD age=31";
//}

type Customer record {
    readonly int id;
    readonly string name;
    string lname;
};

string cutomerListString = "id=13 name=Sanjiva lname=Weerawarana\nid=23 name=James lname=Clark";

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
    CustomerTableWithKS tab = table [{ id: 13 , name: "Sanjiva", lname: "Weerawarana" },
                                    { id: 23 , name: "James" , lname: "Clark" }];

    assertEquality(cutomerListString, tab.toString());
}

function testTableConstructorWithKeySpecifier() {
    CustomerTableWithKS tab = table key(id) [{ id: 13 , name: "Sanjiva", lname: "Weerawarana" },
                                    { id: 23 , name: "James" , lname: "Clark" }];

    assertEquality(cutomerListString, tab.toString());
}

type CustomerTableWithCKS table<Customer> key(id, name);

function testTableTypeWithCompositeKeySpecifier() {
    CustomerTableWithCKS tab = table [{ id: 13 , name: "Sanjiva", lname: "Weerawarana" },
                                    { id: 23 , name: "James" , lname: "Clark" }];

    assertEquality(cutomerListString, tab.toString());
}

function testTableConstructorWithCompositeKeySpecifier() {
    CustomerTableWithCKS tab = table key(id, name) [{ id: 13 , name: "Sanjiva", lname: "Weerawarana" },
                                    { id: 23 , name: "James" , lname: "Clark" }];

    assertEquality(cutomerListString, tab.toString());
}

type CustomerTableWithKTC table<Customer> key<int>;

function testTableTypeWithKeyTypeConstraint() {
    CustomerTableWithKTC tab = table key(id) [{ id: 13 , name: "Sanjiva", lname: "Weerawarana" },
                                    { id: 23 , name: "James" , lname: "Clark" }];

    assertEquality(cutomerListString, tab.toString());
}

type CustomerTableWithCKTC table<Customer> key<[int, string]>;

function testTableTypeWithCompositeKeyTypeConstraint() {
    CustomerTableWithCKTC tab = table key(id, name) [{ id: 13 , name: "Sanjiva", lname: "Weerawarana" },
                                    { id: 23 , name: "James" , lname: "Clark" }];

    assertEquality(cutomerListString, tab.toString());
}

function runMemberAccessTestCases() {
    testMemberAccessWithSingleStringKey();
    testMemberAccessWithSingleIntKey();
    testMemberAccessWithMultiKeyAsTuple();
    testMemberAccessWithMultiKey();
}

function testMemberAccessWithSingleStringKey() {
    table<Customer> key(name) customerTable = table [{ id: 13 , name: "Sanjiva", lname: "Weerawarana" },
                                        { id: 23 , name: "James" , lname: "Clark" }];

    Customer customer = customerTable["Sanjiva"];
    assertEquality("Weerawarana", customer["lname"]);
}

function testMemberAccessWithSingleIntKey() {
    table<Customer> key(id) customerTable = table [{ id: 13 , name: "Sanjiva", lname: "Weerawarana" },
                                        { id: 23 , name: "James" , lname: "Clark" }];

    Customer customer = customerTable[13];
    assertEquality("Weerawarana", customer["lname"]);
}

function testMemberAccessWithMultiKeyAsTuple() {
    table<Customer> key(id, name) customerTable = table [{ id: 13 , name: "Sanjiva", lname: "Weerawarana" },
                                        { id: 23 , name: "James" , lname: "Clark" }];

    Customer customer = customerTable[[13, "Sanjiva"]];
    assertEquality("Weerawarana", customer["lname"]);
}

function testMemberAccessWithMultiKey() {
    table<Customer> key(id, name) customerTable = table [{ id: 13 , name: "Sanjiva", lname: "Weerawarana" },
                                        { id: 23 , name: "James" , lname: "Clark" }];

    Customer customer = customerTable[13, "Sanjiva"];
    assertEquality("Weerawarana", customer["lname"]);
}

function testKeylessTable() {
    table<Customer> customerTable = table [{ id: 13 , name: "Sanjiva", lname: "Weerawarana" },
                                        { id: 23 , name: "James" , lname: "Clark" },
                                        { id: 23 , name: "James" , lname: "Clark" }];

    assertEquality(3, customerTable.length());
    string expectedValues = "id=13 name=Sanjiva lname=Weerawarana\nid=23 name=James lname=Clark\nid=23 name=James lname=Clark";
    assertEquality(expectedValues, customerTable.toString());
}

function testMemberAccessWithInvalidSingleKey() {
    table<Customer> key(id) customerTable = table [{ id: 13 , name: "Sanjiva", lname: "Weerawarana" },
                                        { id: 23 , name: "James" , lname: "Clark" }];

    Customer customer = customerTable[18];
}

function testMemberAccessWithInvalidMultiKey() {
    table<Customer> key(id, name) customerTable = table [{ id: 13 , name: "Sanjiva", lname: "Weerawarana" },
                                        { id: 23 , name: "James" , lname: "Clark" }];

    Customer customer = customerTable[18, "Mohan"];
}

function runTableTestcasesWithVarType() {
    testSimpleTableInitializationWithVarType();
    testTableWithKeySpecifier();
    testTableWithMultiKeySpecifier1();
    testTableWithMultiKeySpecifier2();
    testInferTableType();
}

function testSimpleTableInitializationWithVarType() {
    var customerTable = table [{ id: 13 , name: "Sanjiva", lname: {name: "Weerawarana"}, address: xml `<city>COL</city>` },
                                        { id: 23 , name: "James" , lname: {name:"Clark"} , address: xml `<city>BNK</city>`}];

    assertEquality(2, customerTable.length());
}

function testTableWithKeySpecifier() {
    var customerTable = table key(id) [{ id: 13 , name: "Sanjiva", lname: {name: "Weerawarana"}, address: xml `<city>COL</city>` },
                                        { id: 23 , name: "James" , lname: {name:"Clark"} , address: xml `<city>BNK</city>`}];

    assertEquality(2, customerTable.length());
    assertEquality("Sanjiva", customerTable[13]["name"]);
}

function testInferTableType() {
    string cutomerListString = "id=13 name=Sanjiva lname=Weerawarana\nid=23 name=James\nid=133 name=Mohan lname=Darshan address=Colombo";
    var tab = table [{ id: 13 , name: "Sanjiva", lname: "Weerawarana" },
                                        { id: 23 , name: "James" },
                                       { id: 133 , name: "Mohan", lname: "Darshan" , address: "Colombo"} ];

    assertEquality(cutomerListString, tab.toString());
}

function testTableWithMultiKeySpecifier1() {
    var customerTable1 = table key(id, name) [{ id: 13 , name: "Sanjiva", lname: {name: "Weerawarana"}, address: xml `<city>COL</city>` },
                                        { id: 23 , name: "James" , lname: {name:"Clark"} , address: xml `<city>BNK</city>`}];

    CustomerTableWithCKTC customerTable2 = table key(id, name)
            [{ id: 13 , name: "Sanjiva", lname: "Weerawarana", "address": xml `<city>COL</city>` },
             { id: 23 , name: "James" , lname: "Clark" , "address": xml `<city>BNK</city>`}];

    assertEquality(2, customerTable1.length());
    var lname1 = customerTable1[13, "Sanjiva"]["lname"];
    assertEquality("Weerawarana", lname1["name"]);

    assertEquality(2, customerTable2.length());
    var lname2 = customerTable2[13, "Sanjiva"]["lname"];
    assertEquality("Weerawarana", lname2);
}

function testTableWithMultiKeySpecifier2() {
    table<Customer> key<int> cusTable = table key(id)
            [{ id: 13 , name: "Sanjiva", lname: "Weerawarana"},
             { id: 23 , name: "James" , lname: "Clark"}];

    //TODO: Need to fix here. At this line, compiler passes even if the key type is incompatible
    table<Customer> key<int> customerTable = cusTable;

    assertEquality(2, customerTable.length());
    var lname = customerTable[13]["lname"];
    assertEquality("Weerawarana", lname);
}

function testVarTypeTableInvalidMemberAccess() {
    var customerTable = table key(id, name) [{ id: 13 , name: "Sanjiva", lname: "Weerawarana" },
                                        { id: 23 , name: "James" , lname: "Clark" }];

    Customer customer = customerTable[18, "Mohan"];
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
