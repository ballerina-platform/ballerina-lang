// @type IO1 < O1
type IO1 isolated object {
    int x;
    function f() returns int;
};

// @type IO1 < IO2
// @type IO2 <> O1
type IO2 isolated object {
    int x;
};

type O1 object {
    int x;
    function f() returns int;
};

// @type SO1 < O1
type SO1 service object {
    int x;
    function f() returns int;
};

// @type ISO1 < O1
// @type ISO1 < IO1
// @type ISO1 < SO1
type ISO1 isolated service object {
    int x;
    function f() returns int;
};

// @type CO1 < O1
// @type CO1 <> SO1
type CO1 client object {
    int x;
    function f() returns int;
};

// @type ICO1 < O1
// @type ICO1 < IO1
// @type ICO1 < CO1
// @type ICO1 <> SO1
// @type ICO1 <> ISO1
type ICO1 isolated client object {
    int x;
    function f() returns int;
};

// @type I_TOP < TOP
// @type IO1 < I_TOP
type I_TOP isolated object {};

// @type S_TOP < TOP
// @type SO1 < S_TOP
type S_TOP service object {};

// @type C_TOP < TOP
// @type CO1 < C_TOP
type C_TOP client object {};

type TOP object {};
