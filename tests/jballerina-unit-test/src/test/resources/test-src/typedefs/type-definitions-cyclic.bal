type A int|A[];

public function testCycleTypeArray() {
    A a = 1;
    A b = [1];
    A c = [a];

    assert(a is int, true);
    assert(<int> a, 1);

    int m = 0;
    int n = 0;

    if b is A[] {
        m = <int> b[0];
    }

    if c is A[] {
       n = <int> c[0];
    }

    assert(m, 1);
    assert(n, 1);
}

type B int|map<B>;

public function testCycleTypeMap() {
    B number = 1;
    B mapping = {
        one: number,
        two: 2
    };

    int numberResult = 0;
    if(mapping is map<B>) {
        numberResult = <int> mapping.get("one");
    }
    assert(numberResult, 1);
}

type Person record {
    int id;
    string name;
};

type C Person|table<map<C>>;

public function testCycleTypeTable() {
    table<map<C>> tb = table [
            {
                one: { id: 1, name: "Jane"}
            },
            {
                one: { id: 1, name: "Anne"}
            }
    ];

    string names = "";
    foreach var x in tb {
        var  a = x.get("one");
        if(a is Person) {
            names = names + a.name;
        }
     }
    assert(names, "JaneAnne");
}

type D map<D>|int;

public function testCyclicAsFunctionParams() {
    D d = {
        elem: 2
    };

    map<D> c = <map<D>> takeCyclicTyDef(d);
    assert(<int> c.get("elem"), 5);
}

function takeCyclicTyDef(D d) returns D {
   if(d is map<D>) {
       d["elem"] = 5;
   }
   return d;
}

type E int|record{E a = 1;};

public function testCyclicTypeDefInRecord() {
    E a = 3;
    E rec = { a : a};
    int result = 0;
    if (rec is record{}) {
        result = <int> rec.a;
    }
    assert(result, 3);
}

type F int|string|F[]|map<F>;

public function testCyclicTypeDefInTuple() {
    [F, F, F] values = [1, "hello", [2, "hi"]];
    assert(<int> values[0], 1);
    assert(<string> values[1], "hello");
    var arr = values[2];

    int x = 0;
    string y = "";
    if (arr is F[]) {
        x = <int> arr[0];
        y = <string> arr[1];
    }
    assert(x, 2);
    assert(y, "hi");
}

type H int|string|H[]|map<H>|table<map<H>>|record { H a = "rec"; float x;};

public function testComplexCyclicUnion() {
     H num = 1;
     H str = "hello";
     H mapping = {
         first: num,
         second: str
     };
     H rec = { a : "rec2", x: 2.0};
     H array = [
        1, "hello", mapping, rec
     ];

     int x = 0;
     string y = "";
     string mapVal = "";
     string objVal = "";
     record { H a; float x; } re = { a : "r", x: 0};

     if (array is H[]) {
        x = <int> array[0];
        y = <string> array[1];

        var m = array[2];
        if (m is map<H>) {
            mapVal = <string> m.get("second");
        }

        var n = array[3];
        if (n is record{}) {
            re = <record { H a; float x; }> array[3];
        }
     }
     assert(x, 1);
     assert(y, "hello");
     assert(mapVal, "hello");
     assert(re.a, "rec2");
}

type I H;

function testCyclicUserDefinedType() {
    I i = 1;
    assert(<int> i, 1);
}

function testCyclicUnionAgainstSubSetNegative() {
    record {} x = {};
    assert(false, x is record {| int|boolean|decimal|float|string|xml?...; |});
}

function assert(anydata expected, anydata actual) {
    if (expected != actual) {
        typedesc<anydata> expT = typeof expected;
        typedesc<anydata> actT = typeof actual;
        string reason = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
        error e = error(reason);
        panic e;
    }
}
