type X1 record {|
    int x;
|};

type X2 record {|
    string x;
|};

type T1 table<X1>;
type T2 table<X2>;
type T3 T1|T2;
// @type T3 < T4
type T4 table<X1|X2>;


type X3 map<int>;
type X4 map<string>;

type T5 table<X3>;
type T6 table<X4>;
type T7 T5|T6;
// @type T7 < T8
type T8 table<X3|X4>;

// @type T8 < T9
// @type T7 < T9
type T9 table<map<int|string>>;
