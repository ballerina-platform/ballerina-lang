// @type Z < Y1
// @type Z <> T
type R record {|
    int id;
    int f;
|};

type R1 record {|
    int id;
    int f;
    float d;
|};

type Z table<R1> & !readonly;
type T table<R> & readonly | table<R1> & readonly;
