type M1 map<int|string>;
type M2 map<int|float>;

// @type T1 = M1M2
type M1M2 map<int>;
type T1 M1 & M2;

type M3 map<decimal|int>;

// @type T2 = M1M2
type T2 M1 & M2 & M3;
