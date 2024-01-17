// T1<:TU
// T2<:TU

type R1 record {|
    int id;
    string f;
|};

type R2 record {|
    int id;
    string f;
    float d;
|};

type T1 table<R1>;
type T2 table<R2>;

type TU T1|T2;
