type A anydata;

// @type T < A
type T table<map<boolean|int|float|decimal|string>>;

// @type TB < A
type TB table<map<boolean>>;

// @type TI < A
type TI table<map<int>>;

// @type TARR < A
type TARR table<map<int[]>>;

// @type TANY <> A
type TANY table<map<any>>;

// @type TERR <> A
type TERR table<map<error>>;
