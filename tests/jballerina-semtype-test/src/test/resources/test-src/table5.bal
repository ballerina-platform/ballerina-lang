// T2<:T1
// X2<:X1
type X1 record {|
    int id;
    string f;
|};

type X2 record {|
    int id;
    string:Char f;
|};

type T1 table<X1>;
type T2 table<X2>;
