type R record {|
    int f1;
    boolean f2;
|};

// @type A1 = T1 
type A1 int[];
type T1 [int...];

// @type A2 = T2
type A2 R[];
type T2 [R...];

// @type A3 = T3
type A3 T2[];
type T3 [T2...];
