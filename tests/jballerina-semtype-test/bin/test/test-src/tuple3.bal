// T3<:A3
// A3<:T3

type A3 T2[];

type T3 [T2...];

type T2 [R...];

type R record {|
    int f1;
    boolean f2;
|};
