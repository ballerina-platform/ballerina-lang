// W<:Y1
type R1 record {|
    int id;
    int f;
    float d;
|};

type W table<R1> & readonly;
type Y1 table<R1>;
