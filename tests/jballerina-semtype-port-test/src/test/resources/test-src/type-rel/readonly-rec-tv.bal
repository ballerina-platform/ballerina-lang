// @type R1 = R3
type R1 readonly & record {|
    R2 r2;
|};

type R2 record {|
    R1[] r1s;
|};

type R3 readonly & record {|
    readonly (R2 & readonly) r2;
|};
