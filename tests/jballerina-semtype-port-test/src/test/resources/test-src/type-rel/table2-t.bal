type R1 record {|
    int id;
    string f;
|};

type R2 record {|
    int id;
    string f;
    float d;
|};

type R3 record {|
    int id;
    string:Char f;
|};

type READ readonly;

type T1 table<R1>;
type T2 table<R2>;
type T3 table<R3>;

// @type TI < T1
// @type TI = T3
// @type T1 < TU
// @type T2 < TU
type TI T1 & T3;
type TU T1|T2;

// -@type T1 <> TC
type TC !T1;
