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

// @type W < T
// @type W < READ
// @type T < READ
// @type W < Y1
// @type Y1 <> T
// -@type Z < Y1
// -@type Z <> T
type T table<R> & readonly | table<R1> & readonly;
type W table<R1> & readonly;
type Y1 table<R1>;
type Y table<R>;
type Z table<R1> & !readonly;
