type ImmutableIntArray int[] & readonly;
type IntArray int[];
type IntOrBoolean int|boolean;
type Foo string;
type FooBar "foo"|1;
type FunctionTypeOne function (int i) returns string;
type FunctionTypeTwo function () returns string;

function testTypeReferenceNegative() {
    //ImmutableIntArray intArr = ["A", "B"];
Foo ff = true;
    //typedesc<string[]|boolean[]> b = ImmutableIntArray;
    //typedesc<string> c = ImmutableIntArray;
    //typedesc<string[]> d = Foo;
    //typedesc<string> e = FooBar;
    //typedesc<string> f = FunctionTypeOne;
    //typedesc<function (int i) returns string> g = FunctionTypeTwo;
}
