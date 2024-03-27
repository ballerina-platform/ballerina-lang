// NN<:U
// NN<:UU
// NS<:U
// NS<:UU
// SN<:U
// SN<:UU
// SS<:U
// SS<:UU
// U<:UU

type NN record {|
    int x;
    int y;
|};

type SS record {|
    string x;
    string y;
|};

type NS record {|
    int x;
    string y;
|};

type SN record {|
    string x;
    int y;
|};

type UU record {|
    int|string x;
    int|string y;
|};

type U NN|SS|NS|SN;
