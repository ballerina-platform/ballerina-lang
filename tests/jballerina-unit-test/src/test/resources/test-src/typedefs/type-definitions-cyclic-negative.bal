type A A;

type B map<B>;

type C D|map<C>|int;
type D C|int;

type E int|record{E a = "";};

type CyclicDecimal decimal|CyclicDecimal[];
type tupleCyclic [int, tupleCyclic[]];

function testEuqlity() {
    CyclicDecimal x = 1.0;
    boolean bool = x == 1.23f;
    bool = x != 1.23f;
    bool = x === 1.23f;

    tupleCyclic a = [1];
    bool = a == [2];
    bool = a != [2];
    bool = a === [2];
}

type G [G];
type H [int, H];
type I [int, I[]];
type F [int, string, F|int, map<F>];
type Q [Q...];

function testTupleCyclic() {
    int|F values = [1, "hello", [2, "hi"]];
    I a = [1];
    I b = [a];
}

type R[int, R[1], v...];

type P XNil|XUnion1|XAny|XListRef;
const XNil = "nil";
const XAny = "any";
type XUnion1 "union"|P;

type S [XNil|S[]|XListRef];

type J [int, J[2]];
type J [int, map<J>];

type K string|int|K[];
type K map<K>|string|K[]|int;

type T [XNil, "text1", xInt, L[1]];
const xInt = 1;
type L [int, L[2], T[1]];
type M [int, L[]];

type N [int, N[2]];
type N int|N[];

function testFillerValueCyclicTuple() {
    M a = [1];
    L b = [1];
    T c = ["nil"];
}

type CT record {
    ET i;
};

type DT CT;
type ET DT;

function testCyclicTypeDef() {
      CT t = {
            i : 2
        };
}

type A1 B1 & readonly;
type B1 stream<A1>;

type A2 table<B2>;
type B2 A2 & readonly;

type A3 map<A3?>;
type MyIntersection2 A3 & readonly;

type MyIntersection3 A4 & readonly;
type A4 map<MyIntersection3>;

type A5 A5[];
type MyIntersection4 A5 & readonly;

type A6 xml<A6>;
type MyIntersection5 A6 & readonly;

type A7 table<A7>;
type MyIntersection6 A7 & readonly;

type A8 A8[] & readonly;

type A9 map<A9> & readonly;

type A10 future<A10>;

type A11 table<A11> & readonly;

type A12 xml<A12> & readonly;

type A13 typedesc<A13> & readonly;
