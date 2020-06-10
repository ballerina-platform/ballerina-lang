function runKeySpecifierTestCases() {
    testTableTypeWithXMLKeySpecifierV1();
    testTableTypeWithXMLKeySpecifierV2();
    testTableTypeWithXMLKeyTypeConstraint();
    testTableTypeWithXMLKeyAndVar();
}

type Customer record {
    readonly xml id;
    readonly Name name;
    string address;
};

type Name record {
    string fname;
    string lname;
};

type CustomerTableWithKS table<Customer> key(id);
type CustomerTableWithoutKS table<Customer>;
type CustomerTableWithKC table<Customer> key<xml>;
string tableAsString = "id=<id>123</id> name=fname=Sanjiva lname=Weerawarana address=Sri Lanka\nid=<id>234</id> name=fname=James lname=Clark address=Thailand";

function testTableTypeWithXMLKeySpecifierV1() {
    CustomerTableWithKS tab = table [{id: xml `<id>123</id>`, name: {fname: "Sanjiva", lname: "Weerawarana"}, address: "Sri Lanka" },
                                        {id: xml `<id>234</id>`, name: {fname: "James" , lname: "Clark"}, address: "Thailand" }];

     assertEquality(tableAsString, tab.toString());
}

function testTableTypeWithXMLKeySpecifierV2() {
    CustomerTableWithoutKS tab = table key(id) [{id: xml `<id>123</id>`, name: {fname: "Sanjiva", lname: "Weerawarana"}, address: "Sri Lanka" },
                                        {id: xml `<id>234</id>`, name: {fname: "James" , lname: "Clark"}, address: "Thailand" }];

     assertEquality(tableAsString, tab.toString());
}

function testTableTypeWithXMLKeyTypeConstraint() {
    CustomerTableWithKC tab = table key(id) [{id: xml `<id>123</id>`, name: {fname: "Sanjiva", lname: "Weerawarana"}, address: "Sri Lanka" },
                                        {id: xml `<id>234</id>`, name: {fname: "James" , lname: "Clark"}, address: "Thailand" }];

     assertEquality(tableAsString, tab.toString());
}

function testTableTypeWithXMLKeyAndVar() {
    var tab = table key(id) [{id: xml `<id>123</id>`, name: {fname: "Sanjiva", lname: "Weerawarana"}, address: "Sri Lanka" },
                                        {id: xml `<id>234</id>`, name: {fname: "James" , lname: "Clark"}, address: "Thailand" }];

     assertEquality(tableAsString, tab.toString());
}

function runMemberAccessTestCases() {
    testMemberAccessWithSingleXMLRecordKey();
    testMemberAccessWithXMLMultiKeyAsTuple();
    testMemberAccessWithXMLMultiKey();
}

function testMemberAccessWithSingleXMLRecordKey() {
    table<Customer> key(id) customerTable = table [{id: xml `<id>123</id>`, name: {fname: "Sanjiva", lname: "Weerawarana"}, address: "Sri Lanka" },
                                                   {id: xml `<id>234</id>`, name: {fname: "James" , lname: "Clark"}, address: "Thailand" }];

    Customer customer = customerTable[xml `<id>123</id>`];
    assertEquality("Sri Lanka", customer["address"]);
}

function testMemberAccessWithXMLMultiKeyAsTuple() {
    table<Customer> key(id, name) customerTable = table [{id: xml `<id>123</id>`, name: {fname: "Sanjiva", lname: "Weerawarana"}, address: "Sri Lanka" },
                                                          {id: xml `<id>234</id>`, name: {fname: "James" , lname: "Clark"}, address: "Thailand" }];

    Customer customer = customerTable[[xml `<id>123</id>`, {fname: "Sanjiva", lname: "Weerawarana"}]];
    assertEquality("Sri Lanka", customer["address"]);
}

function testMemberAccessWithXMLMultiKey() {
    table<Customer> key(id, name) customerTable = table [{id: xml `<id>123</id>`, name: {fname: "Sanjiva", lname: "Weerawarana"}, address: "Sri Lanka" },
                                                         {id: xml `<id>234</id>`, name: {fname: "James" , lname: "Clark"}, address: "Thailand" }];

    Customer customer = customerTable[xml `<id>123</id>`, {fname: "Sanjiva", lname: "Weerawarana"}];
    assertEquality("Sri Lanka", customer["address"]);
}

function testMemberAccessWithInvalidXMLRecordKey() {
    table<Customer> key(id) customerTable = table [{id: xml `<id>123</id>`, name: {fname: "Sanjiva", lname: "Weerawarana"}, address: "Sri Lanka" },
                                                   {id: xml `<id>234</id>`, name: {fname: "James" , lname: "Clark"}, address: "Thailand" }];

    Customer customer = customerTable[xml `<id>245</id>`];
}

function testMemberAccessWithInvalidXMLMultiKey() {
    table<Customer> key(id, name) customerTable = table [{id: xml `<id>123</id>`, name: {fname: "Sanjiva", lname: "Weerawarana"}, address: "Sri Lanka" },
                                                   {id: xml `<id>234</id>`, name: {fname: "James" , lname: "Clark"}, address: "Thailand" }];

    Customer customer = customerTable[xml `<id>245</id>`, {fname: "Sanjiva", lname: "Weerawarana"}];
}

function runTableTestcasesWithVarType() {
    testSimpleTableInitializationWithVarType();
    testTableWithKeySpecifier();
    testInferTableType();
    testInferTableTypeV2();
}

function testSimpleTableInitializationWithVarType() {
    var customerTable = table [{id: xml `<id>123</id>`, name: {fname: "Sanjiva", lname: "Weerawarana"}, address: "Sri Lanka" },
                                      {id: xml `<id>234</id>`, name: {fname: "James" , lname: "Clark"}, address: "Thailand" }];

    assertEquality(2, customerTable.length());
}

function testTableWithKeySpecifier() {
    var customerTable = table key(id) [{id: xml `<id>123</id>`, name: {fname: "Sanjiva", lname: "Weerawarana"}, address: "Sri Lanka" },
                                       {id: xml `<id>234</id>`, name: {fname: "James" , lname: "Clark"}, address: "Thailand" }];

    assertEquality(2, customerTable.length());
    assertEquality("Sri Lanka", customerTable[xml `<id>123</id>`]["address"]);
}

function testInferTableType() {
    string cutomerListString = "id=<id>123</id> name=Sanjiva lname=Weerawarana\nid=<id>1233</id> name=James\nid=<id>444</id> name=Mohan lname=Darshan address=no=32 road=High level";
    var tab = table [{ id: xml `<id>123</id>` , name: "Sanjiva", lname: "Weerawarana" },
                                        { id: xml `<id>1233</id>` , name: "James" },
                                       { id: xml `<id>444</id>` , name: "Mohan", lname: "Darshan" , address: {no: 32, road: "High level"}}];

    assertEquality(cutomerListString, tab.toString());
}

function testInferTableTypeV2() {
    var tb = table [
                {id: xml `<id>1233</id>`, name: "Mary", salary: 100.0, address: {no: 32, road: "High level"}},
                {id: xml `<id>453</id>`, name: "Jo", age: 12}
            ];

    tb.put({id: xml `<id>1233</id>`, name: "Pope", salary: 200.0, age: 19, address: {no: 12, road: "Sea street"}});
    assertEquality(3, tb.length());
}

type AssertionError error;

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

    panic AssertionError("AssertionError", message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
