// @type R11 < R12
type R11 record {| int a; |};

type R12 record {| int a; anydata...; |};

// @type R11 < R21
// @type R21 < R22
type R21 record {| int a?; |};

// @type R11 < R22
// @type R12 < R22
type R22 record {| int a?; anydata...; |};

// @type R11 < R31
// @type R31 < R32
type R31 record {| int? a; |};

// @type R11 < R32
// @type R12 < R32
type R32 record {| int? a; anydata...; |};

// @type R41 < R22
// @type R41 < R42
type R41 record {| int a?; string b; |};

// @type R42 < R22
type R42 record {| int a?; string b; anydata...; |};

// @type R51 < R12
// @type R51 < R22
// @type R51 < R32
// @type R51 < R41
// @type R51 < R42
// @type R51 < R52
type R51 record {| int a; string b; |};

// @type R52 < R12
// @type R52 < R22
// @type R52 < R32
// @type R52 < R42
type R52 record {| int a; string b; anydata...; |};

// @type R11 < R61
// @type R21 < R61
// @type R61 < R22
// @type R41 < R61
// @type R51 < R61
// @type R61 < R62
type R61 record {| int a?; string b?; |};

// @type R11 < R62
// @type R21 < R62
// @type R41 < R62
// @type R42 < R62
// @type R51 < R62
// @type R52 < R62
// @type R62 < R22
type R62 record {| int a?; string b?; anydata...; |};

// @type R11 < R71
// @type R71 < R12
// @type R71 < R22
// @type R71 < R32
// @type R71 < R61
// @type R71 < R62
// @type R51 < R71
// @type R71 < R72
type R71 record {| int a; string b?; |};

// @type R11 < R72
// @type R72 < R12
// @type R72 < R22
// @type R72 < R32
// @type R51 < R72
// @type R52 < R72
// @type R72 < R62
type R72 record {| int a; string b?; anydata...; |};

// @type R11 < R81
// @type R21 < R81
// @type R81 < R82
type R81 record {| int|string a?; |};

// @type R11 < R82
// @type R12 < R82
// @type R21 < R82
// @type R22 < R82
// @type R42 < R82
// @type R41 < R82
// @type R51 < R82
// @type R52 < R82
// @type R61 < R82
// @type R62 < R82
// @type R71 < R82
// @type R72 < R82
type R82 record {| int|string a?; anydata...; |};

// @type R11 < R91
// @type R21 < R91
// @type R41 < R91
// @type R51 < R91
// @type R61 < R91
// @type R71 < R91
// @type R81 < R91
// @type R91 < R82
// @type R91 < R92
type R91 record {| int|string a?; string|boolean b?; boolean c?; |};

// @type R11 < R92
// @type R21 < R92
// @type R41 < R92
// @type R51 < R92
// @type R61 < R92
// @type R71 < R92
// @type R81 < R92
// @type R92 < R82
type R92 record {| int|string a?; string|boolean b?; boolean c?; anydata...; |};

// @type R11 < R101
// @type R21 < R101
// @type R31 < R101
// @type R101 < R102
type R101 record {| int? a?; |};

// @type R11 < R102
// @type R12 < R102
// @type R21 < R102
// @type R22 < R102
// @type R31 < R102
// @type R41 < R102
// @type R42 < R102
// @type R51 < R102
// @type R52 < R102
// @type R61 < R102
// @type R62 < R102
// @type R71 < R102
// @type R72 < R102
type R102 record {| int? a?; anydata...; |};

// @type M1 < R21
// @type M1 < R22
// @type M1 < R61
// @type M1 < R62
// @type M1 < R81
// @type M1 < R82
// @type M1 < R91
// @type M1 < R92
// @type M1 < R101
// @type M1 < R102
// @type M1 < M2
type M1 map<never>;

// @type R11 < M2
// @type R12 < M2
// @type R21 < M2
// @type R22 < M2
// @type R31 < M2
// @type R32 < M2
// @type R41 < M2
// @type R42 < M2
// @type R51 < M2
// @type R52 < M2
// @type R61 < M2
// @type R62 < M2
// @type R71 < M2
// @type R72 < M2
// @type R81 < M2
// @type R82 < M2
// @type R91 < M2
// @type R92 < M2
// @type R101 < M2
// @type R102 < M2
type M2 map<anydata>;
