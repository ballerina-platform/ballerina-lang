
function testTableConstructExprs() {
    testTableConstructExprWithAnyMap();
    testTableConstructExprWithAnydataMap();
    testTableConstructExprVar();
}

type CustomerTable table<map<any>>;

function testTableConstructExprWithAnyMap() {

    CustomerTable customerTable = table [{ id: 13 , name: "Sanjiva", lname: "Weerawarana" },
                                                { id: 23 , name: "James" , lname: "Clark" }];

    assertEquality("[{\"id\":13,\"name\":\"Sanjiva\",\"lname\":\"Weerawarana\"},{\"id\":23,\"name\":\"James\",\"lname\":\"Clark\"}]", customerTable.toString());
}

type TableWithAnydata table<map<anydata>>;

function testTableConstructExprWithAnydataMap() {

    TableWithAnydata customerTable = table [{ id: 13 , name: "Sanjiva", lname: "Weerawarana" },
                                                { id: 23 , name: "James" , lname: "Clark" }];

    assertEquality("[{\"id\":13,\"name\":\"Sanjiva\",\"lname\":\"Weerawarana\"},{\"id\":23,\"name\":\"James\",\"lname\":\"Clark\"}]", customerTable.toString());
}

function testTableConstructExprVar() {
    map<int> m = {a: 1, b: 2};

    var v1 = table [
        {name: "Jo"},
        {...m}
    ];

    table<record {|string name?; int...;|}> t1 = v1;

    assertEquality("[{\"name\":\"Jo\"},{\"a\":1,\"b\":2}]", t1.toString());
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
