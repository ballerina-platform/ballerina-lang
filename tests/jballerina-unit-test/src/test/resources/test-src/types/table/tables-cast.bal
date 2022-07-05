type Person record {
    readonly string name;
    readonly int age;
    string country;
};

type Teacher record {
    readonly string name;
    readonly int age;
    string school;
};

type Customer record {
    readonly int id;
    readonly string name;
    string lname;
};

type CustomerTable table<Customer|Teacher>;

type PersonTable1 table<Person|Customer> key<string>;

type PersonTable2 table<Person|Teacher> key<string|int>;

type PersonTable3 table<Teacher> key<int> | table<Person> key<int>;

PersonTable1 tab1 = table key(name) [
    { name: "AAA", age: 31, country: "LK" },
    { name: "BBB", age: 34, country: "US" }
    ];

PersonTable2 tab2 = table key(age) [
    { name: "AAA", age: 31, country: "LK" },
    { name: "BBB", age: 34, country: "US" },
    { name: "BBB", age: 33, school: "MIT" }
    ];

CustomerTable tab3 = table key(name) [
    { id: 13 , name: "Foo", lname: "QWER" },
    { id: 13 , name: "Bar" , lname: "UYOR" }
    ];

PersonTable3 tab4 = table key(age) [
    { name: "AAA", age: 31, country: "LK" },
    { name: "BBB", age: 34, country: "US" }
    ];

function testKeyConstraintCastToString1() returns boolean {
    table<Person> key<string> tab = <table<Person> key<string>> tab1;
    return tab["AAA"]["name"] == "AAA";
}

function testKeyConstraintCastToString2() returns boolean {
    table<Customer> key(name) tab =<table<Customer> key(name)> tab3;
    return tab["Foo"]["name"] == "Foo";
}

function testKeyConstraintCastToString3() returns boolean {
    table<Person|Teacher> key<int> tab =<table<Person|Teacher> key<int>> tab2;
    return tab[31]["name"] == "AAA";
}

function testKeyConstraintCastToString4() returns boolean {
    table<Person> key<int> tab =<table<Person> key<int>> tab4;
    return tab[31]["name"] == "AAA";
}

type CustomerEmptyKeyedTbl table<Customer> key();

function testCastingWithEmptyKeyedKeylessTbl() {
    table<Customer> key() custbl1 = <table<Customer> key()>tab3;
    assertEquals(table key(name) [
            {id: 13, name: "Foo", lname: "QWER"},
            {id: 13, name: "Bar", lname: "UYOR"}
        ], custbl1);

    CustomerEmptyKeyedTbl custbl2 = <CustomerEmptyKeyedTbl>tab3;
    assertEquals(custbl1, custbl2);

    table<record {
        readonly int id;
        readonly string name;
        string lname;
    }> key() custbl3 = <table<record {
        readonly int id;
        readonly string name;
        string lname;
    }> key()> tab3;
    assertEquals(custbl2, custbl3);

    CustomerEmptyKeyedTbl custbl4 = table [
            {id: 13, name: "Foo", lname: "QWER"},
            {id: 13, name: "Bar", lname: "UYOR"}
        ];
    table<record {
        readonly int id;
        readonly string name;
        string lname;
    }> key() custbl5 = <table<record {
        readonly int id;
        readonly string name;
        string lname;
    }> key()> custbl4;
    assertEquals(table [
            {id: 13, name: "Foo", lname: "QWER"},
            {id: 13, name: "Bar", lname: "UYOR"}
        ], custbl5);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquals(anydata expected, anydata actual) {
    if (expected == actual) {
        return;
    }
    typedesc<anydata> expT = typeof expected;
    typedesc<anydata> actT = typeof actual;
    string msg = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
    panic error(ASSERTION_ERROR_REASON, message = msg);
}
