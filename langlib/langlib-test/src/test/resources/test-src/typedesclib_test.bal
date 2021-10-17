
import ballerina/lang.'typedesc;

type Error distinct error;

type MyError distinct error;

type MyOtherError distinct (Error & error<record {string msg;}>);

type ErrorAndMyOtherError Error & MyOtherError;

type ErrorAndMyOtherErrorD distinct (Error & MyOtherError);

type TwoDistinctKeywords distinct error & distinct error;

type ThreeDistinctKeyworlds distinct error & distinct error & distinct error;

type ThreeDiscintKeyworldsWithGroup (distinct error & distinct error) & distinct error;

public function testGeTypeIds() {
    var tids = MyError.typeIds();
    assertSingleTypeId(tids, ["MyError"]);
    assertReadonlyness(tids);

    var eTids = Error.typeIds();
    assertSingleTypeId(eTids, ["Error"]);
    assertReadonlyness(eTids);

    error e = error MyError("MyError");
    typedesc<error> tdesc = typeof e;
    typedesc:TypeId[]? ids = tdesc.typeIds();
    assertSingleTypeId(ids, ["MyError"]);
    assertReadonlyness(ids);

    MyOtherError oe = error("Hello", msg = "msg");
    tids = (typeof oe).typeIds();
    var k = MyOtherError.typeIds();

    assertSingleTypeId(tids, ["Error", "MyOtherError"]);
    assertSingleTypeId(k, ["Error", "MyOtherError"]);

    k = ErrorAndMyOtherError.typeIds();
    assertSingleTypeId(k, ["Error", "MyOtherError", "Error"]);

    k = ErrorAndMyOtherError.typeIds(true);
    assertSingleTypeId(k, ["Error", "MyOtherError"]);

    k = ErrorAndMyOtherErrorD.typeIds();
    assertSingleTypeId(k, ["Error", "MyOtherError", "ErrorAndMyOtherErrorD"]);

    k = ErrorAndMyOtherErrorD.typeIds(true);
    assertSingleTypeId(k, ["ErrorAndMyOtherErrorD"]);

    k = TwoDistinctKeywords.typeIds();
    assertSingleTypeId(k, [0, 1]);

    k = ThreeDistinctKeyworlds.typeIds();
    assertSingleTypeId(k, [2, 3, 4]);

    k = ThreeDiscintKeyworldsWithGroup.typeIds();
    assertSingleTypeId(k, [5, 6, 7]);

    k = ThreeDiscintKeyworldsWithGroup.typeIds(true);
    assertSingleTypeId(k, [5, 6, 7]);
}

function assertSingleTypeId(typedesc:TypeId[]? tids, (string|int)[] localIds) {
    if (tids is typedesc:TypeId[]) {
        if (tids.length() != localIds.length()) {
            panic error("Assertion error: Expected length of "
            + localIds.length().toString() + ", found: " + tids.toString());
        }

        var tempTids = tids; // type of `tids` is not narrowed within query expression.
        foreach var i in 0 ..< localIds.length() {
            var localId = localIds[i];
            (string|int)[] k = from var id in tempTids where id.localId == localId select localId;
            if (k.length() == 0) {
                panic error("Assertion error: Expected localId: "
                + localId.toString() + " not found in " + tids.toString());
            }
        }
    } else {
        panic error("Assertion error: expected array of: " + typedesc:TypeId.toString() + "found: " + (typeof tids).toString());
    }
}

function assertReadonlyness(any a) {
    if !(a is readonly) {
        panic error("Assertion error: value is not a readonly value");
    }
}