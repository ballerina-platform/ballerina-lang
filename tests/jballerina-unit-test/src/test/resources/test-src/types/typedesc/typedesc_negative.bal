function test1(){
    int;

    typedesc<any> x = int;
    typedesc<any> y = int;
    if (x === y) {
        i = 5;
    }
    typedesc a = byte[];
    typedesc b = int[]|string[];
}

type ImmutableIntArray int[] & readonly;
type IntArray int[];
type Foo "foo";
type FooBar "foo"|1;
type FunctionTypeOne function (int i) returns string;
type FunctionTypeTwo function () returns string;

function testTypeDefReferenceAsTypeDescNegative() {
    typedesc<readonly> a = IntArray;
    typedesc<string[]|boolean[]> b = ImmutableIntArray;
    typedesc<string> c = ImmutableIntArray;
    typedesc<string[]> d = Foo;
    typedesc<string> e = FooBar;
    typedesc<string> f = FunctionTypeOne;
    typedesc<function (int i) returns string> g = FunctionTypeTwo;
}
