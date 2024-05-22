type M1 map<any>|record {};

// @type M1 = M2
type M2 map<any>M|record {};

type M3 map<(1|2|3)>|map<(1|2)>;

// @type M3 = M4
// @type M4 < M1
// @type M4 < M2
// @type M3 < M1
// @type M3 < M2
type M4 map<(1|2|3)>|map<(1|2)>;
