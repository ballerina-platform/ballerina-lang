type R1 record {|
    int id;
    string f;
|};

type R3 record {|
    int id;
    string:Char f;
|};

type T1 table<R1>;
type T3 table<R3>;

// @type TI < T1
// @type TI = T3
type TI T1 & T3;
// @type T1 <> TC
type TC !T1;
