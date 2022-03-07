// T2<:A2
// A2<:T2

type A2 R[];

type T2 [R...];

type R record {|
    int f1;
    boolean f2;
|};
