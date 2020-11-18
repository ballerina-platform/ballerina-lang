function runKeySpecifierTestCases() {
    testTableTypeWithKeySpecifier();
    testTableConstructorWithKeySpecifier();
    testTableTypeWithCompositeKeySpecifier();
    testTableConstructorWithCompositeKeySpecifier();
    testTableTypeWithKeyTypeConstraint();
    testTableTypeWithCompositeKeyTypeConstraint();
}

type Customer record {
    readonly Name name;
    readonly int id;
    string address;
};

type Name record {
    string fname;
    string lname;
};

type CustomerTableWithKS table<Customer> key(name);
string tableAsString = "[{\"name\":{\"fname\":\"Sanjiva\",\"lname\":\"Weerawarana\"},\"id\":13,\"address\":\"Sri Lanka\"},{\"name\":{\"fname\":\"James\",\"lname\":\"Clark\"},\"id\":23,\"address\":\"Thailand\"}]";

function testTableTypeWithKeySpecifier() {
    CustomerTableWithKS tab = table [{name: {fname: "Sanjiva", lname: "Weerawarana"}, id: 13, address: "Sri Lanka" },
                                    {name: {fname: "James" , lname: "Clark"}, id: 23 , address: "Thailand" }];

    assertEquality(tableAsString, tab.toString());
}

function testTableConstructorWithKeySpecifier() {
    CustomerTableWithKS tab = table key(name) [{name: {fname: "Sanjiva", lname: "Weerawarana"}, id: 13, address: "Sri Lanka" },
                                            {name: {fname: "James" , lname: "Clark"}, id: 23 , address: "Thailand" }];

    assertEquality(tableAsString, tab.toString());
}

type CustomerTableWithCKS table<Customer> key(id, name);

function testTableTypeWithCompositeKeySpecifier() {
    CustomerTableWithCKS tab = table [{name: {fname: "Sanjiva", lname: "Weerawarana"}, id: 13, address: "Sri Lanka" },
                                        {name: {fname: "James" , lname: "Clark"}, id: 23 , address: "Thailand" }];

    assertEquality(tableAsString, tab.toString());
}

function testTableConstructorWithCompositeKeySpecifier() {
    CustomerTableWithCKS tab = table key(id, name) [{name: {fname: "Sanjiva", lname: "Weerawarana"}, id: 13, address: "Sri Lanka" },
                                                    {name: {fname: "James" , lname: "Clark"}, id: 23 , address: "Thailand" }];

    assertEquality(tableAsString, tab.toString());
}

type CustomerTableWithKTC table<Customer> key<Name>;

function testTableTypeWithKeyTypeConstraint() {
    CustomerTableWithKTC tab = table key(name) [{name: {fname: "Sanjiva", lname: "Weerawarana"}, id: 13, address: "Sri Lanka" },
                                                {name: {fname: "James" , lname: "Clark"}, id: 23 , address: "Thailand" }];

    assertEquality(tableAsString, tab.toString());
}

type CustomerTableWithCKTC table<Customer> key<[int, Name]>;

function testTableTypeWithCompositeKeyTypeConstraint() {
    CustomerTableWithCKTC tab = table key(id, name) [{name: {fname: "Sanjiva", lname: "Weerawarana"}, id: 13, address: "Sri Lanka" },
                                                {name: {fname: "James" , lname: "Clark"}, id: 23 , address: "Thailand" }];

    assertEquality(tableAsString, tab.toString());
}

function runMemberAccessTestCases() {
    testMemberAccessWithSingleRecordKey();
    testMemberAccessWithMultiKeyAsTuple();
    testMemberAccessWithMultiKey();
}

function testMemberAccessWithSingleRecordKey() {
    table<Customer> key(name) customerTable = table [{name: {fname: "Sanjiva", lname: "Weerawarana"}, id: 13, address: "Sri Lanka" },
                                                {name: {fname: "James" , lname: "Clark"}, id: 23 , address: "Thailand" }];

    Customer customer = customerTable[{fname: "Sanjiva", lname: "Weerawarana"}];
    assertEquality("Sri Lanka", customer["address"]);
}

function testMemberAccessWithMultiKeyAsTuple() {
    table<Customer> key(id, name) customerTable = table [{name: {fname: "Sanjiva", lname: "Weerawarana"}, id: 13, address: "Sri Lanka" },
                                                    {name: {fname: "James" , lname: "Clark"}, id: 23 , address: "Thailand" }];

    Customer customer = customerTable[[13, {fname: "Sanjiva", lname: "Weerawarana"}]];
    assertEquality("Sri Lanka", customer["address"]);
}

function testMemberAccessWithMultiKey() {
    table<Customer> key(id, name) customerTable = table [{name: {fname: "Sanjiva", lname: "Weerawarana"}, id: 13, address: "Sri Lanka" },
                                                        {name: {fname: "James" , lname: "Clark"}, id: 23 , address: "Thailand" }];

    Customer customer = customerTable[13, {fname: "Sanjiva", lname: "Weerawarana"}];
    assertEquality("Sri Lanka", customer["address"]);
}

function testKeylessTable() {
    table<Customer> customerTable = table [{name: {fname: "Sanjiva", lname: "Weerawarana"}, id: 13, address: "Sri Lanka" },
                                        {name: {fname: "James" , lname: "Clark"}, id: 23 , address: "Thailand" },
                                        {name: {fname: "James" , lname: "Clark"}, id: 23 , address: "Thailand" }];

    assertEquality(3, customerTable.length());
    string expectedValues = "[{\"name\":{\"fname\":\"Sanjiva\",\"lname\":\"Weerawarana\"},\"id\":13,\"address\":\"Sri Lanka\"},{\"name\":{\"fname\":\"James\",\"lname\":\"Clark\"},\"id\":23,\"address\":\"Thailand\"},{\"name\":{\"fname\":\"James\",\"lname\":\"Clark\"},\"id\":23,\"address\":\"Thailand\"}]";
    assertEquality(expectedValues, customerTable.toString());
}

function testMemberAccessWithInvalidSingleKey() {
    table<Customer> key(name) customerTable = table [{name: {fname: "Sanjiva", lname: "Weerawarana"}, id: 13, address: "Sri Lanka" },
                                        {name: {fname: "James" , lname: "Clark"}, id: 23 , address: "Thailand" }];

    Customer customer = customerTable[{fname: "Sanjiva" , lname: "Clark"}];
}

function testMemberAccessWithInvalidMultiKey() {
    table<Customer> key(id, name) customerTable = table [{name: {fname: "Sanjiva", lname: "Weerawarana"}, id: 13, address: "Sri Lanka" },
                                        {name: {fname: "James" , lname: "Clark"}, id: 23 , address: "Thailand" }];

    Customer customer = customerTable[18, {fname: "Sanjiva" , lname: "Clark"}];
}

function runTableTestcasesWithVarType() {
    testSimpleTableInitializationWithVarType();
    testTableWithKeySpecifier();
    testInferTableType();
    testInferTableTypeV2();
}

function testSimpleTableInitializationWithVarType() {
    var customerTable = table [{ id: 13 , name: {fname: "Sanjiva", lname: "Weerawarana"}, address: xml `<city>COL</city>` },
                                        { id: 23 , name: {fname: "James" , lname: "Clark"}, address: xml `<city>BNK</city>`}];

    assertEquality(2, customerTable.length());
}

function testTableWithKeySpecifier() {
    var customerTable = table key(id) [{ id: 13 , name: {fname: "Sanjiva", lname: "Weerawarana"}, address: xml `<city>COL</city>` },
                                        { id: 23 , name: {fname: "James" , lname: "Clark"} , address: xml `<city>BNK</city>`}];

    assertEquality(2, customerTable.length());
    Name name = {fname: "Sanjiva", lname: "Weerawarana"};
    assertEquality(name, customerTable[13]["name"]);
}

function testInferTableType() {
    string cutomerListString = "[{\"id\":13,\"name\":\"Sanjiva\",\"lname\":\"Weerawarana\"},{\"id\":23,\"name\":\"James\"},{\"id\":133,\"name\":\"Mohan\",\"lname\":\"Darshan\",\"address\":{\"no\":32,\"road\":\"High level\"}}]";
    var tab = table [{ id: 13 , name: "Sanjiva", lname: "Weerawarana" },
                                        { id: 23 , name: "James" },
                                       { id: 133 , name: "Mohan", lname: "Darshan" , address: {no: 32, road: "High level"}}];

    assertEquality(cutomerListString, tab.toString());
}

function testInferTableTypeV2() {
    var tb = table [
                {id: 1, name: "Mary", salary: 100.0, address: {no: 32, road: "High level"}},
                {id: 2, name: "Jo", age: 12}
            ];

    tb.put({id: 3, name: "Pope", salary: 200.0, age: 19, address: {no: 12, road: "Sea street"}});
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
