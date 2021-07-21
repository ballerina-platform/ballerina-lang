import testorg/foo;
import testorg/foo.records;

const ASSERTION_ERROR_REASON = "AssertionError";

type FunctionTypeDesc typedesc<function () returns (never)>;

function testTypeOfNeverReturnTypedFunction() {
    any|error expectedFunctionType = FunctionTypeDesc;

    typedesc <any|error> actualFunctionType = typeof foo:sigma;

    if (actualFunctionType is typedesc<function () returns (never)>) {
        return;
    }

    string expectedValAsString =
                expectedFunctionType is error ? expectedFunctionType.toString() : expectedFunctionType.toString();
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expectedValAsString + "', found '" + actualFunctionType.toString() + "'");
}

function testNeverReturnTypedFunctionCall() {
    error e = trap foo:sigma();
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
