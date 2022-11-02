function runKeySpecifierTestCases() {
    testTableTypeWithKeySpecifier();
    testTableConstructorWithKeySpecifier();
    testTableTypeWithCompositeKeySpecifier();
    testTableConstructorWithCompositeKeySpecifier();
    testTableTypeWithKeyTypeConstraint();
    testTableTypeWithCompositeKeyTypeConstraint();
    testTableTypeWithMultiFieldKeys();
    testVariableNameFieldAsKeyField();
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

type EmployeeId record {
    readonly string firstname;
    readonly string lastname;
};

type Employee1 record {
    *EmployeeId;
    int leaves;
};

type EmployeeTable table<Employee1> key<EmployeeId>;

function testTableTypeWithMultiFieldKeys() {
    string expected = "[{\"leaves\":10,\"firstname\":\"John\",\"lastname\":\"Wick\"}]";
    table<Employee1> key<EmployeeId> t1 = table key(firstname,lastname) [{firstname: "John", lastname: "Wick", leaves: 10}];
    assertEquality(expected, t1.toString());

    EmployeeTable t2 = table key(firstname,lastname) [{firstname: "John", lastname: "Wick", leaves: 10}];
    assertEquality(expected, t2.toString());
}

function runMemberAccessTestCases() {
    testMemberAccessWithSingleRecordKey();
    testMemberAccessWithMultiKeyAsTuple();
    testMemberAccessWithMultiKey();
}

function testMemberAccessWithSingleRecordKey() {
    table<Customer> key(name) customerTable = table [{name: {fname: "Sanjiva", lname: "Weerawarana"}, id: 13, address: "Sri Lanka" },
                                                {name: {fname: "James" , lname: "Clark"}, id: 23 , address: "Thailand" }];

    Customer? customer = customerTable[{fname: "Sanjiva", lname: "Weerawarana"}];
    assertEquality("Sri Lanka", customer["address"]);
}

function testMemberAccessWithMultiKeyAsTuple() {
    table<Customer> key(id, name) customerTable = table [{name: {fname: "Sanjiva", lname: "Weerawarana"}, id: 13, address: "Sri Lanka" },
                                                    {name: {fname: "James" , lname: "Clark"}, id: 23 , address: "Thailand" }];

    Customer? customer = customerTable[[13, {fname: "Sanjiva", lname: "Weerawarana"}]];
    assertEquality("Sri Lanka", customer["address"]);
}

function testMemberAccessWithMultiKey() {
    table<Customer> key(id, name) customerTable = table [{name: {fname: "Sanjiva", lname: "Weerawarana"}, id: 13, address: "Sri Lanka" },
                                                        {name: {fname: "James" , lname: "Clark"}, id: 23 , address: "Thailand" }];

    Customer? customer = customerTable[13, {fname: "Sanjiva", lname: "Weerawarana"}];
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

    Customer? customer = customerTable[{fname: "Sanjiva" , lname: "Clark"}];
    assertEquality((), customer);
}

function testMemberAccessWithInvalidMultiKey() {
    table<Customer> key(id, name) customerTable = table [{name: {fname: "Sanjiva", lname: "Weerawarana"}, id: 13, address: "Sri Lanka" },
                                        {name: {fname: "James" , lname: "Clark"}, id: 23 , address: "Thailand" }];

    Customer? customer = customerTable[18, {fname: "Sanjiva" , lname: "Clark"}];
    assertEquality((), customer);
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

const int id = 1;

function testVariableNameFieldAsKeyField() {
    table<record {readonly int id; string name;}> key (id) tb = table [
        {id, name: "Jo"},
        {id: 2, name: "Amy"}
    ];
    assertEquality(tb[1], <record {readonly int id; string name;}> {"id":1, "name":"Jo"});
    assertEquality(tb[2], <record {readonly int id; string name;}> {"id":2, "name":"Amy"});
}

function testDefaultValueFieldAsKeyField() {
    table<record {readonly int id = 1; string name;}> key (id) tb = table [
        {id, name: "Jo"},
        {id: 2, name: "Amy"}
    ];
    assertEquality(tb[1], <record {readonly int id = 1; string name;}> {"id":1, "name":"Jo"});
    assertEquality(tb[2], <record {readonly int id = 1; string name;}> {"id":2, "name":"Amy"});
}

type CustomRecord record {
    readonly int id = 1;
    string name;
};

function testDefaultValueFieldAsKeyField2() {
    table<CustomRecord> key (id) tb = table [
        {id, name: "Jo"},
        {id: 2, name: "Amy"}
    ];
    assertEquality(tb[1], <CustomRecord> {"id":1, "name":"Jo"});
    assertEquality(tb[2], <CustomRecord> {"id":2, "name":"Amy"});
}

type CustomRecord2 record {
    readonly int id = 1;
    readonly string status = "status - 1";
    string name;
};

const string status = "status - 1";

function testDefaultValueFieldAsKeyField3() {
    table<CustomRecord2> key (id, status) tb = table [
        {id, status, name: "Jo"},
        {id: 2, status: "status - 2", name: "Amy"}
    ];
    assertEquality(tb[1, "status - 1"], <CustomRecord2> {"id":1, "status":"status - 1", "name":"Jo"});
    assertEquality(tb[2, "status - 2"], <CustomRecord2> {"id":2, "status":"status - 2", "name":"Amy"});
}

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

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("AssertionError",
                        message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
