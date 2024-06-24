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
    public int bar;
}