import tabletypetest.foo as users;

public function getPublicTableInstance() {
    users:UserTable tab = table [{ username: "John", id: 30}];
    assertEquality(1, tab.length());
    assertEquality(tab["John"]?.username, "John");
    assertEquality(tab["John"]?.id, 30);

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
