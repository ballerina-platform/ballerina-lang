// W<:READ
type READ readonly;

type R1 record {|
    int id;
    int f;
    float d;
|};

type W table<R1> & readonly;
