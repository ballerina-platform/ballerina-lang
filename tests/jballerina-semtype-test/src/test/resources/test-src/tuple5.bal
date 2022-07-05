// R<:R1
// R1<:R
// R2<:R
// R2<:R1
// T<:T1
// T1<:T
// T2<:T
// T2<:T1

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

type T [R...];

type T1 [R1...];

type T2 [R2...];
