// @type C1 <> C2
type  C1  client object {
    resource function get [int]();
};

// @type Ci1 <> C1
type  Ci1  client object {
    resource function post [int]();
};

type  C2  client object {
    resource function get [string]();
};

// @type C1 < C3
type  C3  client object {
    resource function get [byte]();
};

type Cx1 client object {
    resource function get foo/[int](int x);
};

// @type Cx1 < Cx2
type Cx2 client object {
    resource function get foo/[byte](byte x);
};

// @type Cx3 <> Cx2
type Cx3 client object {
    resource function get bar/[byte]/bar(byte x);
};
