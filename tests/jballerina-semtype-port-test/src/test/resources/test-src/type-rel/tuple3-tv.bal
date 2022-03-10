type R record {|
    int f1;
    boolean f2;
|};

type R1 record {|
    int f1;
    boolean f2;
|};

type R2 record {|
    int:Signed16 f1;
    boolean f2;
|};

// @type T1 = T
// @type T2 < T
type T [R...];
type T1 [R1...];
type T2 [R2...];
