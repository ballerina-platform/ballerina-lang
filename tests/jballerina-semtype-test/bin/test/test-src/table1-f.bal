// T<:READ
type READ readonly;

type R record {|
    int id;
    int f;
|};

type R1 record {|
    int id;
    int f;
    float d;
|};

type T table<R> & readonly | table<R1> & readonly;

// READ is not a subtype of T
// BUG #34728
