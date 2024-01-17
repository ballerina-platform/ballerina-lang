// T1<:TU
// T2<:TU
// READ<:TU
// The above relation is not correct
// BUG #34737

type R1 record {|
    int id;
    string f;
|};

type R2 record {|
    int id;
    string f;
    float d;
|};

type READ readonly;

type T1 table<R1>;
type T2 table<R2>;

type TU T1|T2;
