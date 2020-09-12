type Person record {
    readonly string name;
    int age;
};

type Foo record {
    readonly map<string> m;
    int age;
};

type GlobalTable1 table<Person> key(name);
type GlobalTable2 table<Foo> key(m);

GlobalTable1 tab1 = table [
  { name: "AAA", age: 31 },
  { name: "BBB", age: 34 }
];

function testGlobalTableConstructExpr() returns boolean {
    return tab1.toString() == "[{\"name\":\"AAA\",\"age\":31},{\"name\":\"BBB\",\"age\":34}]";
}

function testTableMemberAccessStore() returns boolean {
    GlobalTable2 tab = table [
      { m: {"AAA":"DDDD"}, age: 31 },
      { m: {"BBB":"DDDD"}, age: 34 }
    ];

    tab[{"CCC":"EEEE"}] = { m: {"CCC":"EEEE"}, age: 34 };
    return tab.toString() == "[{\"m\":{\"AAA\":\"DDDD\"},\"age\":31},{\"m\":{\"BBB\":\"DDDD\"},\"age\":34},{\"m\":{\"CCC\":\"EEEE\"},\"age\":34}]";
}

function testTableMemberAccessLoad() returns boolean {
    GlobalTable2 tab = table [
      { m: {"AAA":"DDDD"}, age: 31 },
      { m: {"BBB":"DDDD"}, age: 34 }
    ];
    Foo aaa = tab[{"AAA":"DDDD"}];
    return aaa.toString() == "{\"m\":{\"AAA\":\"DDDD\"},\"age\":31}";
}

type Customer record {
    readonly int id;
    readonly string name;
    string lname;
};

string cutomerListString = "[{\"id\":13,\"name\":\"Sanjiva\",\"lname\":\"Weerawarana\"},{\"id\":23,\"name\":\"James\",\"lname\":\"Clark\"}]";

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
    string expectedValues = "[{\"id\":13,\"name\":\"Sanjiva\",\"lname\":\"Weerawarana\"},{\"id\":23,\"name\":\"James\",\"lname\":\"Clark\"},{\"id\":23,\"name\":\"James\",\"lname\":\"Clark\"}]";
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
    testInferTableTypeV2();
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
    string cutomerListString = "[{\"id\":13,\"name\":\"Sanjiva\",\"lname\":\"Weerawarana\"},{\"id\":23,\"name\":\"James\"},{\"id\":133,\"name\":\"Mohan\",\"lname\":\"Darshan\",\"address\":\"Colombo\"}]";
    var tab = table [{ id: 13 , name: "Sanjiva", lname: "Weerawarana" },
                                        { id: 23 , name: "James" },
                                       { id: 133 , name: "Mohan", lname: "Darshan" , address: "Colombo"} ];

    assertEquality(cutomerListString, tab.toString());
}

function testInferTableTypeV2() {
    var tb = table [
                {id: 1, name: "Mary", salary: 100.0},
                {id: 2, name: "Jo", age: 12}
            ];

    tb.put({id: 3, name: "Pope", salary: 200.0, age: 19});
    assertEquality(3, tb.length());
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

type Details record {|
    string name;
    string id;
|};

type TableRec record {|
    table<Details> detTable;
|};

function testTableAsRecordField()  {
    TableRec tableRecord1 = {
            detTable: table [
                {name: "Jo", id: "azqw"},
                {name: "Amy", id: "ldhe"}
            ]
    };

    table<Details> tb = table [
            {name: "Jo", id: "azqw"},
            {name: "Amy", id: "ldhe"}
        ];

     TableRec tableRecord2 = {detTable: tb};

    assertEquality("{\"detTable\":[{\"name\":\"Jo\",\"id\":\"azqw\"},{\"name\":\"Amy\",\"id\":\"ldhe\"}]}", tableRecord1.toString());
    assertEquality("{\"detTable\":[{\"name\":\"Jo\",\"id\":\"azqw\"},{\"name\":\"Amy\",\"id\":\"ldhe\"}]}", tableRecord2.toString());
}

type Bar record {|
    string x;
    string y;
|};

function testTableEquality() {
    testSameTable();
    testIdenticalTable();
    testUnidenticalTable();
    testInEqualityTableV1();
    testTableEqualityWithKey();
    testTableEqualityWithKeyV2();
}

function testSameTable() {
    table<Bar> t1 = table [
                            {x: "x1", y: "y1"},
                            {x: "x2", y: "y2"}
                        ];

    assertEquality(true, t1 == t1);
}

function testIdenticalTable() {
    table<Bar> t1 = table [
                            {x: "x1", y: "y1"},
                            {x: "x2", y: "y2"}
                        ];

    table<Bar> t2 = table [
                            {x: "x1", y: "y1"},
                            {x: "x2", y: "y2"}
                            ];

    assertEquality(true, t1 == t2);
}

function testUnidenticalTable() {
    table<Bar> t1 = table [
                            {x: "x1", y: "y1"},
                            {x: "x2", y: "y2"}
                        ];

    table<Bar> t2 = table [
                            {x: "x1", y: "y1"},
                            {x: "x56", y: "y2"}
                            ];

    assertEquality(false, t1 == t2);
}

function testInEqualityTableV1() {
    table<Bar> t1 = table [
                            {x: "x1", y: "y1"},
                            {x: "x2", y: "y2"}
                        ];

    table<Bar> t2 = table [
                            {x: "x1", y: "y1"},
                            {x: "x56", y: "y2"}
                            ];

    assertEquality(true, t1 != t2);
}

function testInEqualityTableV2() {
    table<Bar> t1 = table [
                            {x: "x1", y: "y1"},
                            {x: "x2", y: "y2"}
                        ];

    table<Bar> t2 = table [
                            {x: "x1", y: "y1"},
                            {x: "x1", y: "y2"}
                            ];

    assertEquality(false, t1 != t2);
}

type Employee record {
    readonly int id;
    readonly string name;
    float salary;
};
type EmployeeTable table<Employee> key(id);

function testTableEqualityWithKey() {

    EmployeeTable employeeTab1 = table [
      {id: 1, name: "John", salary: 300.50},
      {id: 2, name: "Bella", salary: 500.50},
      {id: 3, name: "Peter", salary: 750.0}
    ];

    EmployeeTable employeeTab2 = table [
      {id: 1, name: "John", salary: 300.50},
      {id: 2, name: "Bella", salary: 500.50},
      {id: 3, name: "Peter", salary: 750.0}
    ];

    assertEquality(true, employeeTab1 == employeeTab2);
}

function testTableEqualityWithKeyV2() {

    table<Employee> key(id) employeeTab1 = table [
      {id: 1, name: "John", salary: 300.50},
      {id: 2, name: "Bella", salary: 500.50},
      {id: 3, name: "Peter", salary: 750.0}
    ];

    table<Employee> key(id) employeeTab2 = table [
      {id: 1, name: "John", salary: 300.50},
      {id: 2, name: "Bella", salary: 500.50},
      {id: 3, name: "Ethen", salary: 750.0}
    ];

    assertEquality(true, employeeTab1 != employeeTab2);
}

type AssertionError error;

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

    panic AssertionError(ASSERTION_ERROR_REASON, message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
