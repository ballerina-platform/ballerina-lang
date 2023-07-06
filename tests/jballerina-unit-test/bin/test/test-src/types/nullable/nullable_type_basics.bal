function funcReturnNil() {
}

function testNullableTypeBasics1() returns int? {
    int? i = funcReturnNil();
    i = ();
    i = 5;
    return i;
}

function testNullableTypeBasics2() returns int? {
    int? i = funcReturnNil();
    return i;
}

function testNullableArrayTypes1() returns any {
    float?[] fa = [1.0, 5.0, 3.0, ()];
    float? f = fa[0];
    return f;
}

type SearchResultType RESULT_TYPE_MIXED|RESULT_TYPE_RECENT|RESULT_TYPE_POPULAR;

const RESULT_TYPE_MIXED = "mixed";
const RESULT_TYPE_RECENT = "recent";
const RESULT_TYPE_POPULAR = "popular";

function testNilableTypeInTypeTest() returns string {
    SearchResultType? s = RESULT_TYPE_MIXED;

    if (s is SearchResultType) {
        return <string>s;
    }

    return "()";
}

function testNullWithBasicTypes() {
    int? a = null;
    string? b = null;
    decimal? c = null;
    boolean? d = null;
    float? e = null;
    assertEquality(true, a is null);
    assertEquality(true, b is null);
    assertEquality(true, c is null);
    assertEquality(true, d is null);
    assertEquality(true, e is null);
}

function testNullWithMap() {
    map<string>? a = null;
    map<json>? b = null;
    assertEquality(true, a is null);
    assertEquality(true, b is null);
}

function testNullWithMap2() {
    map<string?> a = {"a": null};
    map<json> b = {"b": null};
    assertEquality(true, a["a"] is null);
    assertEquality(true, b["b"] is null);
}

function testNullWithArray() {
    string[]? a = null;
    json[]? b = null;
    string?[] c = [null, "ABC", "DEF"];
    assertEquality(true, a is null);
    assertEquality(true, b is null);
    assertEquality(true, c[0] is null);
    assertEquality("ABC", c[1]);
    assertEquality("DEF", c[2]);
}

type A A[]|();
type B B[]|map<string>;
type C C[]|map<int?>;
type D string?|int;
type E string|map<string?>;
type Person record {| string name; |};

function testNullWithType() {
    A a = null;
    B? b = null;
    C c = {"c": null};
    D d = null;
    E? e = null;
    map<Person>? f = null;
    Person? g = null;
    Person[]? h= null;
    Person?[] i = [null , {"name": "John"}];

    assertEquality(true, a is null);
    assertEquality(true, b is null);
    assertEquality(true, c is map<int?>);
    if c is map<int?> {
        assertEquality(true, c["c"] is null);
    }
    assertEquality(true, d is null);
    assertEquality(true, e is null);
    assertEquality(true, f is null);
    assertEquality(true, g is null);
    assertEquality(true, h is null);
    assertEquality(true, i[0] is null);
}

type AssertionError distinct error;

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error AssertionError(ASSERTION_ERROR_REASON,
            message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
