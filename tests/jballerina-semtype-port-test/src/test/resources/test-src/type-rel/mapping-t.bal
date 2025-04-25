// @type T < S
public type T R1|map<"A">;

public type R1 record {|
    byte A;
    float...;
|};

public type S R2|map<string>;

public type R2 record {|
    int A;
    float...;
|};

// @type T2504 < T2525
public type T2504 [map<[int]>, map<1>];

public type T2525 (map<int[]>|map<int>)[];


// @type MISI < MIS
type MISI map<int>|map<string>;

type MIS map<int|string>;
