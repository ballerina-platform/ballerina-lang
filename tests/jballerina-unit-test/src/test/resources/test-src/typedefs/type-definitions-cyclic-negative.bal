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
