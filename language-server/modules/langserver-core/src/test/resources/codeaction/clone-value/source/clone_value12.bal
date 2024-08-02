type Rec record {|
    map<string> mp;
    int i;
|};

isolated record {|record {} rec;|} rec1 = {rec: {}};
isolated Rec rec2 = {mp: {}, i: 0};
isolated record {|Rec field1; record {|int id; Rec value;|} field2;|} rec3 = {
    field1: {mp: {}, i: 0},
    field2: {id: 0, value: {mp: {}, i: 0}}
};

isolated function fn1(record {} a) {
    lock {
        rec1.rec = a;
    }
}

function fn2(map<string> a) {
    lock {
        rec2.mp = a;
    }
}

function fn3(map<string> a) {
    lock {
        rec3.field1.mp = a;
    }
}

function fn4(map<string> a) {
    lock {
        rec3.field2.value.mp = a;
    }
}

function fn5(Rec a) {
    lock {
        rec2 = a;
    }
}
