import testorg/foo;
import testorg/records;

const ASSERTION_ERROR_REASON = "AssertionError";

function testTypeOfNeverReturnTypedFunction() {
    any|error expectedFunctionType = typedesc<function () returns (never)>;

    typedesc <any|error> actualFunctionType = typeof foo:sigma;

    if (actualFunctionType is typedesc<function () returns (never)>) {
        return;
    }
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expectedFunctionType.toString() + "', found '" + actualFunctionType.toString () + "'");
}

function testNeverReturnTypedFunctionCall() {
    foo:sigma();
}

function testInclusiveRecord() {
    records:VehicleWithNever vehicle = {j:0, "q":1};
}

function testExclusiveRecord() {
    records:ClosedVehicleWithNever closedVehicle = {j:0};
}

type SomePersonalTable table<records:SomePerson> key<never>;

function testNeverWithKeyLessTable() {
    SomePersonalTable personalTable = table [
        { name: "DD", age: 33},
        { name: "XX", age: 34}
    ];
}
