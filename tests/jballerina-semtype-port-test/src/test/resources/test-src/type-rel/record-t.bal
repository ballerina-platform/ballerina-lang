// the order of type defns are intentional

type T1 record {|
    65 X;
|};

type T2 record {|
    int:Signed8 X;
|};

// @type T1 < T3
type T3 T4|T2;

type T4 record {|
    int:Signed8 a;
|};
