type T1 A[];

type T2 [B, C];

type T3 map<D>;

type T4 E;

type T5 record { F f; };

type T6 object { G g; };

type T7 int[]|A[]|[B, C]|map<string>|map<D>|E|int|record { F f; }|object { G g; }|error;

type T8 [int[], A[], [B, C], map<string>, map<D>, E, int, record { F f; }, object { G g; }, error];

type PersonOrInt int|Person;
type PersonOrNil Person?;

type InvalidIntersectionType int & string;

function foo() {
    type MyType int;
}

type CustomType int;

type CustomType record {
    int i = 0;
};

type MyTuple [int, string];

function bar() returns MyTuple => [1, ""];

function testTypeReference() {
    function () returns int x = bar;
}

type Foo boolean|null;

function testNullFiniteType() {
    Foo _ = "null"; // error
}

class H {};

H res = check new I();

int[] a = [1, 2, 3, 4, 5];

int[] b = from var i in a select <I> i;

float result = <J>1 + 2.0;

int result2 = true? <J>1 : 2;

var result3 = <H> new J();
