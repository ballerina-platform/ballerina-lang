// T2<:T1
// T<:READ
// W<:READ
// W<:T
// W<:Y1
// X2<:X1

type R record {|
    int id;
    int f;
|};

type R1 record {|
    int id;
    int f;
    float d;
|};

type READ readonly;

type T table<R> & readonly | table<R1> & readonly;
type W table<R1> & readonly;
type Y1 table<R1>;

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
