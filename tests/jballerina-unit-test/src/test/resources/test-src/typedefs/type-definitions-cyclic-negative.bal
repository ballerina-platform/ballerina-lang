type A A;

type B map<B>;

type C D|map<C>|int;
type D C|int;

type E int|record{E a = "";};

type CyclicDecimal decimal|CyclicDecimal[];

function testEuqlity() {
    CyclicDecimal x = 1.0;
    boolean bool = x == 1.23f;
    bool = x != 1.23f;
    bool = x === 1.23f;
}
