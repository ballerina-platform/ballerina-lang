// @type R1 < M1
type R1 record {| int x; int...; |};
type M1 map<int>;

// @type R3 = R1
type R3 record {| int x; int ...; |};

// @type R4 < M2
// @type R1 < M2
// @type M1 < M2
type R4 record {| int|string x; int|string ...; |};
type M2 map<int|string>;

// @type R5 < M1
// @type R5 < R1
type R5 record {| int x; |};

// @type R6 < R1
// @type R6 < M1
type R6 record {| int x; int y; int ...; |};

// @type R7 <> R6
type R7 record {| int x; int y; string ...; |};

// @type R6 < R8
type R8 record {| int x; int y; int|string ...; |};

// @type R9 <> R8
type R9 record {| int j; string k; |};

// @type R10 < R8
type R10 record {| int x; int y; string j; |};
