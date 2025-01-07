// the order of type defns are intentional

type M map<any>;

// @type M = N
type N map<any>;

// @type M1 = N
// @type M1 = M
type M1 M|record {};

// @type M1 = M2
// @type M2 = M
// @type M2 = N
type M2 N|record {};
