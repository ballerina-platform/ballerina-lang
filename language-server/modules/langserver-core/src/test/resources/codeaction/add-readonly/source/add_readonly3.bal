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

isolated function fn1() returns record {} {
    lock {
        return rec1.rec;
    }
}

function fn2() returns map<string> {
    lock {
        return rec2.mp;
    }
}

function fn3() returns map<string> {
    lock {
        return rec3.field1.mp;
    }
}

function fn4() returns map<string> {
    lock {
        return rec3.field2.value.mp;
    }
}
