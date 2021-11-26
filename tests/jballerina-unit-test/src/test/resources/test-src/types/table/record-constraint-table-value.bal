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

    tab.add({ m: {"CCC":"EEEE"}, age: 34 });
    return tab.toString() == "[{\"m\":{\"AAA\":\"DDDD\"},\"age\":31},{\"m\":{\"BBB\":\"DDDD\"},\"age\":34},{\"m\":{\"CCC\":\"EEEE\"},\"age\":34}]";
}

function testTableMemberAccessLoad() returns boolean {
    GlobalTable2 tab = table [
      { m: {"AAA":"DDDD"}, age: 31 },
      { m: {"BBB":"DDDD"}, age: 34 }
    ];
    Foo? aaa = tab[{"AAA":"DDDD"}];
    return aaa.toString() == "{\"m\":{\"AAA\":\"DDDD\"},\"age\":31}";
}

type Customer record {
    readonly int id;
    readonly string name;
    string lname;
};

string cutomerListString = "[{\"id\":13,\"name\":\"Sanjiva\",\"lname\":\"Weerawarana\"},{\"id\":23,\"name\":\"James\",\"lname\":\"Clark\"}]";

type CustomerTableWithKS table<Customer> key(id);

type Student record {|
    readonly string name;
    int id?;
    Address address;
|};

type Address record {
    string city;
    string? country;
};

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

    Customer? customer = customerTable["Sanjiva"];
    assertEquality("Weerawarana", customer["lname"]);
}

function testMemberAccessWithSingleIntKey() {
    table<Customer> key(id) customerTable = table [{ id: 13 , name: "Sanjiva", lname: "Weerawarana" },
                                        { id: 23 , name: "James" , lname: "Clark" }];

    Customer? customer = customerTable[13];
    assertEquality("Weerawarana", customer["lname"]);
}

function testMemberAccessWithMultiKeyAsTuple() {
    table<Customer> key(id, name) customerTable = table [{ id: 13 , name: "Sanjiva", lname: "Weerawarana" },
                                        { id: 23 , name: "James" , lname: "Clark" }];

    Customer? customer = customerTable[[13, "Sanjiva"]];
    assertEquality("Weerawarana", customer["lname"]);
}

function testMemberAccessWithMultiKey() {
    table<Customer> key(id, name) customerTable = table [{ id: 13 , name: "Sanjiva", lname: "Weerawarana" },
                                        { id: 23 , name: "James" , lname: "Clark" }];

    Customer? customer = customerTable[13, "Sanjiva"];
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

    Customer? customer = customerTable[18];
    assertEquality((), customer);
}

function testMemberAccessWithInvalidMultiKey() {
    table<Customer> key(id, name) customerTable = table [{ id: 13 , name: "Sanjiva", lname: "Weerawarana" },
                                        { id: 23 , name: "James" , lname: "Clark" }];

    Customer? customer = customerTable[18, "Mohan"];
    assertEquality((), customer);
}

function runTableTestcasesWithVarType() {
    testSimpleTableInitializationWithVarType();
    testTableWithKeySpecifier();
    testTableWithMultiKeySpecifier1();
    testTableWithMultiKeySpecifier2();
    testInferTableType();
    testInferTableTypeV2();
    testTableConstructExprVar();
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

    Customer? customer = customerTable[18, "Mohan"];
    assertEquality((), customer);
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

function testMemberAccessHavingNilableFields() {
    table<Student> key(name) tab = table [
        {name: "Amy", id: 1234, address:{"street": "Main Street", "city": "Colombo", "country": ()}},
        {name: "John", address:{"city": "Colombo", "country": "Sri Lanka"}}
    ];

    int? val1 = tab["John"]["id"];
    assertEquality(true, val1 is ());

    string? val2 = tab["Amy"]["address"]["country"];
    assertEquality(true, val2 is ());

    anydata val3 = tab["John"]["address"]["street"];
    assertEquality(true, val3 is ());

    int? val4 = tab["Mike"]["id"];
    assertEquality(true, val4 is ());
}

type Emp record {
    readonly string name;
    string department;
};

type UnionConstraint Person|Emp;

type UnionConstrinedTbl table<UnionConstraint> key(name);

function testUnionConstrainedTableIteration() {
    UnionConstrinedTbl tbl = table key(name)[
      { name: "Adam", age: 33 },
      { name: "Mark", department: "HR" },
      { name: "John", age: 40 }
    ];

    string[] names = [];
    string[] expectedNames = ["Adam", "John"];

    int i = -1;
    foreach var rec in tbl {
      if rec is Person {
         i += 1;
         names[i] = rec.name;
      }
    }

    assertEquality(expectedNames, names);
}

type FooRec record {
    readonly int x;
    int y;
};

public function testSpreadFieldInConstructor() {
    string expected = "[{\"x\":1001,\"y\":20},{\"x\":1002,\"y\":30}]";
    FooRec spreadField1 = {x: 1002, y: 30};

    table<FooRec> tb1 = table key(x) [
            {x: 1001, y: 20},
            {...spreadField1}
        ];
    assertEquality(expected, tb1.toString());

    var spreadField2 = {x: 1002, y: 30};

    var tb2 = table key(x) [
            {x: 1001, y: 20},
            {...spreadField2}
        ];
    assertEquality(expected, tb2.toString());

    var spreadField3 = {id: 2, name: "Jo", age: 12};
    var tb3 = table [
            {id: 1, name: "Mary", salary: 100.0},
            {...spreadField3}
        ];
    assertEquality("[{\"id\":1,\"name\":\"Mary\",\"salary\":100.0},{\"id\":2,\"name\":\"Jo\",\"age\":12}]",
    tb3.toString());
}

function testTableConstructExprVar() {
    string s1 = "id";
    string s2 = "employed";

    var v1 = table [
        {name: "Jo"},
        {[s1]: 2},
        {[s2]: false}
    ];

    table<record {|(string|int|boolean) name?; int|boolean...;|}> t1 = v1;
    assertEquality("[{\"name\":\"Jo\"},{\"id\":2},{\"employed\":false}]", t1.toString());
}

function testTableTypeInferenceWithVarType() {
    testTableTypeInferenceWithVarType1();
    testTableTypeInferenceWithVarType2();
    testTableTypeInferenceWithVarType3();
    testTableTypeInferenceWithVarType4();
    testTableTypeInferenceWithVarType5();
}

function testTableTypeInferenceWithVarType1() {
    var v1 = table [
            {a: 1},
            {a: "str", b: 2}
        ];

    table<record {|int|string a; int b?;|}> _ = v1;
    v1.add({a: 1});
    v1.add({a: "str", b: 2});
}

function testTableTypeInferenceWithVarType2() {
    record {|string a; int b?;|} m = {a: "str", b: 2};
    var v1 = table [
            {a: 1},
            {...m},
            {a: true, c: 2, b: false}
        ];

    table<record {|(int|string|boolean) a; (int|boolean) b?; int c?;|}> _ = v1;
    v1.add({a: 1});
    v1.add({...m});
    v1.add({a: true, c: 2, b: false});
}

function testTableTypeInferenceWithVarType3() {
    record {|string|boolean a; int b?;|} m = {a: "str", b: 2};
    var v1 = table [
            {a: 1},
            {...m}
        ];

    table<record {|(int|string|boolean) a; int b?;|}> _ = v1;
    assertFalse(v1 is table<record {|(int|string) a; int b?;|}>);
    v1.add({a: 1});
    v1.add({...m});
}

function testTableTypeInferenceWithVarType4() {
    record {|string|boolean a; int|json b?;|} m = {a: "str", b: 2};
    var v1 = table [
            {a: 1, b: "c"},
            {...m}
        ];

    table<record {|(int|string|boolean) a; (int|string|json) b?;|}> _ = v1;
    assertFalse(v1 is table<record {|(int|string) a; (int|string) b?;|}>);
    v1.add({a: 1, b: "c"});
    v1.add({...m});
}

function testTableTypeInferenceWithVarType5() {
    record {string a; int b?;} m = {a: "str", b: 2};
    string s1 = "a";
    string s2 = "b";

    var v1 = table [
            {a: 1},
            {...m},
            {[s1] : true, c: 2, [s2] : false}
        ];

    table<record {|int|string|boolean a?; int|boolean b?; anydata c?; anydata...; |}> _ = v1;
    v1.add({a: 1});
    v1.add({...m});
    v1.add({[s1] : true, c: 2, [s2] : false});
}

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

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                        message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
