type ImmutableIntArray int[] & readonly;
type IntArray int[];
type IntOrBoolean int|boolean;
type Foo string;
type FooBar "foo"|1;
type FunctionTypeOne function (int i) returns string;
type FunctionTypeTwo function () returns string;

function testTypeReferenceNegative() {
    ImmutableIntArray intArr = "A";

    Foo foo = true;

    FunctionTypeOne func1 = function (IntOrBoolean x) returns int {
            return 1;
        };

    FunctionTypeTwo func2 = function (FooBar i) returns int {
        return 1;
    };

    var func3 = function (Foo foo1) returns int {
        return 1;
    };
    var res1 = func3(10);

    var func4 = function (Foo foo2) returns Foo {
        return 1;
    };
    var res2 = func4("foo");

    string _ = getImmutable();
}

function getImmutable() returns ImmutableIntArray {
    return [1,2, 3];
}
