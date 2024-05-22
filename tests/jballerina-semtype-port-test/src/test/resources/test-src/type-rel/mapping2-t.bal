// the order of type defns are intentional

type M map<(1|2|3)>;

// @type M = N
type N map<(1|2|3)>;

// @type M1 = N
// @type M1 = M
type M1 M|map<(1|2)>;

// @type M1 = M2
// @type M2 = M
// @type M2 = N
type M2 N|map<(1|2)>;
