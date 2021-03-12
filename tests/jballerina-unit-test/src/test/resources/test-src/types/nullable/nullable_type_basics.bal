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

function testNullWithBasicTypes() returns [int?, string?, decimal?, boolean?, float?] {
    int? a = null;
    string? b = null;
    decimal? c = null;
    boolean? d = null;
    float? e = null;
    return [a, b, c, d, e];
}

function testNullWithMap() returns [map<string>?, map<json>?] {
    map<string>? a = null;
    map<json>? b = null;
    return [a, b];
}

function testNullWithMap2() returns [map<string?>, map<json>] {
    map<string?> a = {"a": null};
    map<json> b = {"b": null};
    return [a, b];
}

function testNullWithArray() returns [string[]?, json[]?, string?[]] {
    string[]? a = null;
    json[]? b = null;
    string?[] c = [null, "ABC", "DEF"];
    return [a, b, c];
}
