// @type OD1 < O1
// @type OD2 < O1
// @type OD1 <> OD2
// @type OD3 < OD1

type OD1 distinct O1;

type OD2 distinct O1;

type OD3 distinct OD1;

type O1 object {
    int foo;
    function bar() returns int;
};

// @type ORD1 < OR
// @type ORD1 <> ORD2
type ORD1 distinct object {
    ORD1? oo;
};

type ORD2 distinct OR;

type OR object {
    OR? oo;
};
