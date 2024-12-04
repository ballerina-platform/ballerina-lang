// @type O1 = O2
type O1 object {
    public int a;
};

type O2 object {
    public int a;
};

// @type O3 < O1
type O3 object {
    public int a;
    public string b;
};

// @type O4 < O1
type O4 object {
    public byte a;
};

// @type OO1 = OO2
type OO1 object {
    public function foo(int a) returns int;
};

type OO2 object {
    public function foo(int a) returns int;
};

// @type OO3 < OO1
type OO3 object {
    public function foo(int a, int... rest) returns int;
};

// @type OO4 < OO1
type OO4 object {
    public function foo(int a) returns int;
    public int a;
};

// @type OO5 <> OO4
type OO5 object {
    public function (int a) returns int foo;
    public int a;
};

// @type G3 <> O3
// @type G3 < O1
type G3 object {
    public int a;
    string b;
};

// @type OO4 <> GG4
// @type GG4 < O1
type GG4 object {
    function foo(int a) returns int;
    public int a;
};

// @type I1 < OO1
type I1 object {
    public isolated function foo(int a) returns int;
}
